package com.hivemq.extensions.handler;

import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.interceptor.suback.SubackOutboundInterceptor;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.task.PluginInOutTask;
import com.hivemq.extensions.executor.task.PluginInOutTaskContext;
import com.hivemq.extensions.interceptor.suback.parameter.SubackOutboundInputImpl;
import com.hivemq.extensions.interceptor.suback.parameter.SubackOutboundOutputImpl;
import com.hivemq.mqtt.message.suback.SUBACK;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Atherton
 */
@Singleton
@ChannelHandler.Sharable
public class SubackOutboundInterceptorHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SubackOutboundInterceptorHandler.class);

    private final @NotNull FullConfigurationService configurationService;

    private final @NotNull PluginOutPutAsyncer asyncer;

    private final @NotNull HiveMQExtensions hiveMQExtensions;

    private final @NotNull PluginTaskExecutorService executorService;

    @Inject
    public SubackOutboundInterceptorHandler(
            final @NotNull FullConfigurationService configurationService,
            final @NotNull PluginOutPutAsyncer asyncer,
            final @NotNull HiveMQExtensions hiveMQExtensions,
            final @NotNull PluginTaskExecutorService executorService) {
        this.configurationService = configurationService;
        this.asyncer = asyncer;
        this.hiveMQExtensions = hiveMQExtensions;
        this.executorService = executorService;
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg, final @NotNull ChannelPromise promise)
            throws Exception {

        if (!(msg instanceof SUBACK)) {
            ctx.write(msg, promise);
            return;
        }
        handleOutboundSuback(ctx, (SUBACK) msg, promise);
    }

    private void handleOutboundSuback(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull SUBACK suback,
            final @NotNull ChannelPromise promise) {

        final Channel channel = ctx.channel();

        final String clientId = channel.attr(ChannelAttributes.CLIENT_ID).get();
        if (clientId == null) {
            return;
        }

        final ClientContextImpl clientContext = channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).get();
        if (clientContext == null) {
            ctx.write(suback, promise);
            return;
        }
        final List<SubackOutboundInterceptor> interceptors = clientContext.getSubackOutboundInterceptors();
        if (interceptors.isEmpty()) {
            ctx.write(suback, promise);
            return;
        }

        final SubackOutboundOutputImpl output = new SubackOutboundOutputImpl(configurationService, asyncer, suback);
        final SubackOutboundInputImpl input = new SubackOutboundInputImpl(clientId, channel, suback);

        final SubAckOutboundInterceptorContext interceptorContext =
                new SubAckOutboundInterceptorContext(
                        SubAckOutboundInterceptorTask.class, clientId, input, ctx, promise, interceptors.size());

        for (final SubackOutboundInterceptor interceptor : interceptors) {

            final HiveMQExtension extension = hiveMQExtensions.getExtensionForClassloader(
                    (IsolatedPluginClassloader) interceptor.getClass().getClassLoader());

            if (extension == null) {
                interceptorContext.increment(output);
                continue;
            }

            final SubAckOutboundInterceptorTask interceptorTask =
                    new SubAckOutboundInterceptorTask(interceptor, extension.getId());

            executorService.handlePluginInOutTaskExecution(interceptorContext, input, output, interceptorTask);
        }
    }

    private static class SubAckOutboundInterceptorContext extends PluginInOutTaskContext<SubackOutboundOutputImpl> {

        private final @NotNull SubackOutboundInputImpl input;
        private final @NotNull ChannelHandlerContext ctx;
        private final @NotNull ChannelPromise promise;
        private final int interceptorCount;
        private final @NotNull AtomicInteger counter;

        public SubAckOutboundInterceptorContext(
                @NotNull final Class<?> taskClazz,
                @NotNull final String identifier,
                @NotNull final SubackOutboundInputImpl input,
                @NotNull final ChannelHandlerContext ctx,
                @NotNull final ChannelPromise promise,
                final int interceptorCount) {
            super(taskClazz, identifier);
            this.input = input;
            this.ctx = ctx;
            this.promise = promise;
            this.interceptorCount = interceptorCount;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void pluginPost(
                final @NotNull SubackOutboundOutputImpl output) {
            if (output.isTimedOut()) {
                log.debug("Async timeout on outbound SUBACK interception.");
                output.update(input.getSubackPacket());
            } else if (output.getSubackPacket().isModified()) {
                input.update(output.getSubackPacket());
            }
            increment(output);
        }

        public void increment(final @NotNull SubackOutboundOutputImpl output) {
            if (counter.incrementAndGet() == interceptorCount) {
                final SUBACK finalSuback = SUBACK.createSubAckFrom(output.getSubackPacket());
                ctx.writeAndFlush(finalSuback, promise);
            }
        }
    }

    private static class SubAckOutboundInterceptorTask
            implements PluginInOutTask<SubackOutboundInputImpl, SubackOutboundOutputImpl> {

        private final @NotNull SubackOutboundInterceptor interceptor;
        private final @NotNull String pluginId;

        public SubAckOutboundInterceptorTask(
                @NotNull final SubackOutboundInterceptor interceptor,
                @NotNull final String pluginId) {
            this.interceptor = interceptor;
            this.pluginId = pluginId;
        }


        @Override
        public @NotNull SubackOutboundOutputImpl apply(
                final @NotNull SubackOutboundInputImpl input,
                final @NotNull SubackOutboundOutputImpl output) {
            try {
                interceptor.onOutboundSuback(input, output);
            } catch (final Throwable e) {
                log.warn(
                        "Uncaught exception was thrown from extension with id \"{}\" on outbound subAck interception. " +
                                "Extensions are responsible to handle their own exceptions.", pluginId);
                log.debug("Original exception: ", e);
                output.update(input.getSubackPacket());
            }
            return output;
        }

        @Override
        public @NotNull ClassLoader getPluginClassLoader() {
            return interceptor.getClass().getClassLoader();
        }
    }
}
