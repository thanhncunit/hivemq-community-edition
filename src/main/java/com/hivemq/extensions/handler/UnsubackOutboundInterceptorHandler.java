package com.hivemq.extensions.handler;

import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.interceptor.unsuback.UnsubackOutboundInterceptor;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.task.PluginInOutTask;
import com.hivemq.extensions.executor.task.PluginInOutTaskContext;
import com.hivemq.extensions.interceptor.unsuback.parameter.UnsubackOutboundInputImpl;
import com.hivemq.extensions.interceptor.unsuback.parameter.UnsubackOutboundOutputImpl;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Atherton
 */
@Singleton
@ChannelHandler.Sharable
public class UnsubackOutboundInterceptorHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(UnsubackOutboundInterceptorHandler.class);

    private final @NotNull FullConfigurationService configurationService;

    private final @NotNull PluginOutPutAsyncer asyncer;

    private final @NotNull HiveMQExtensions hiveMQExtensions;

    private final @NotNull PluginTaskExecutorService executorService;

    public UnsubackOutboundInterceptorHandler(
            @NotNull final FullConfigurationService configurationService,
            @NotNull final PluginOutPutAsyncer asyncer,
            @NotNull final HiveMQExtensions hiveMQExtensions,
            @NotNull final PluginTaskExecutorService executorService) {
        this.configurationService = configurationService;
        this.asyncer = asyncer;
        this.hiveMQExtensions = hiveMQExtensions;
        this.executorService = executorService;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, @NotNull final Object msg, @NotNull final ChannelPromise promise)
            throws Exception {
        super.write(ctx, msg, promise);

        if (!(msg instanceof UNSUBACK)) {
            ctx.write(msg, promise);
            return;
        }
        handleOutboundUnsuback(ctx, (UNSUBACK) msg, promise);
    }

    private void handleOutboundUnsuback(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull UNSUBACK unsuback,
            final @NotNull ChannelPromise promise) {

        final Channel channel = ctx.channel();

        final String clientId = channel.attr(ChannelAttributes.CLIENT_ID).get();
        if (clientId == null) {
            return;
        }

        final ClientContextImpl clientContext = channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).get();
        if (clientContext == null) {
            ctx.write(unsuback, promise);
            return;
        }

        final List<UnsubackOutboundInterceptor> interceptors = clientContext.getUnsubackOutboundInterceptors();
        if (interceptors.isEmpty()) {
            ctx.write(unsuback, promise);
            return;
        }

        final UnsubackOutboundOutputImpl output =
                new UnsubackOutboundOutputImpl(configurationService, asyncer, unsuback);
        final UnsubackOutboundInputImpl input = new UnsubackOutboundInputImpl(clientId, channel, unsuback);

        final UnsubackOutboundInterceptorContext interceptorContext =
                new UnsubackOutboundInterceptorContext(
                        UnsubackOutboundInterceptorTask.class, clientId, input, ctx, promise, interceptors.size());

        for (final UnsubackOutboundInterceptor interceptor : interceptors) {
            final HiveMQExtension extension = hiveMQExtensions.getExtensionForClassloader(
                    (IsolatedPluginClassloader) interceptor.getClass().getClassLoader());

            if (extension == null) {
                interceptorContext.increment(output);
                continue;
            }

            final UnsubackOutboundInterceptorTask interceptorTask =
                    new UnsubackOutboundInterceptorTask(interceptor, extension.getId());

            executorService.handlePluginInOutTaskExecution(interceptorContext, input, output, interceptorTask);
        }
    }

    private static class UnsubackOutboundInterceptorContext extends PluginInOutTaskContext<UnsubackOutboundOutputImpl> {

        private final @NotNull UnsubackOutboundInputImpl input;
        private final @NotNull ChannelHandlerContext ctx;
        private final @NotNull ChannelPromise promise;
        private final int interceptorCount;
        private final @NotNull AtomicInteger counter;

        public UnsubackOutboundInterceptorContext(
                @NotNull final Class<?> taskClazz,
                @NotNull final String identifier,
                @NotNull final UnsubackOutboundInputImpl input,
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
                final @NotNull UnsubackOutboundOutputImpl output) {
            if (output.isTimedOut()) {
                log.debug("Async timeout on outbound SUBACK interception.");
                output.update(input.getUnsubackPacket());
            } else if (output.getUnsubackPacket().isModified()) {
                input.update(output.getUnsubackPacket());
            }
            increment(output);

        }

        public void increment(final @NotNull UnsubackOutboundOutputImpl output) {
            if (counter.incrementAndGet() == interceptorCount) {
                final UNSUBACK unsuback = UNSUBACK.createUnsubackFrom(output.getUnsubackPacket());
                ctx.writeAndFlush(unsuback, promise);
            }
        }
    }

    private static class UnsubackOutboundInterceptorTask
            implements PluginInOutTask<UnsubackOutboundInputImpl, UnsubackOutboundOutputImpl> {

        private final @NotNull UnsubackOutboundInterceptor interceptor;
        private final @NotNull String extensionId;

        public UnsubackOutboundInterceptorTask(
                @NotNull final UnsubackOutboundInterceptor interceptor,
                @NotNull final String extensionId) {
            this.interceptor = interceptor;
            this.extensionId = extensionId;
        }

        @Override
        @NotNull
        public UnsubackOutboundOutputImpl apply(
                final @NotNull UnsubackOutboundInputImpl input,
                final @NotNull UnsubackOutboundOutputImpl output) {
            try {
                interceptor.onOutboundUnsuback(input, output);
            } catch (final Throwable e) {
                log.warn(
                        "Uncaught exception was thrown from extension with id \"{}\" on outbound UNSUBACK interception. " +
                                "Extensions are responsible to handle their own exceptions.", extensionId);
                log.debug("Original exception: ", e);
                output.update(input.getUnsubackPacket());
            }
            return output;
        }

        @Override
        public @NotNull ClassLoader getPluginClassLoader() {
            return interceptor.getClass().getClassLoader();
        }
    }
}
