package com.hivemq.extensions.handler;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.unsubscribe.UnsubscribeInboundInterceptor;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.task.PluginInOutTask;
import com.hivemq.extensions.executor.task.PluginInOutTaskContext;
import com.hivemq.extensions.interceptor.unsubscribe.parameter.UnsubscribeInboundInputImpl;
import com.hivemq.extensions.interceptor.unsubscribe.parameter.UnsubscribeInboundOutputImpl;
import com.hivemq.extensions.packets.unsubscribe.UnsubscribePacketImpl;
import com.hivemq.mqtt.message.unsubscribe.UNSUBSCRIBE;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
public class UnsubscribeInboundInterceptorHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(UnsubscribeInboundInterceptorHandler.class);

    private final @NotNull FullConfigurationService configurationService;
    private final @NotNull PluginOutPutAsyncer asyncer;
    private final @NotNull HiveMQExtensions hiveMQExtensions;
    private final @NotNull PluginTaskExecutorService pluginTaskExecutorService;

    @Inject
    public UnsubscribeInboundInterceptorHandler(
            @NotNull final FullConfigurationService configurationService,
            @NotNull final PluginOutPutAsyncer asyncer,
            @NotNull final HiveMQExtensions hiveMQExtensions,
            @NotNull final PluginTaskExecutorService pluginTaskExecutorService) {
        this.configurationService = configurationService;
        this.asyncer = asyncer;
        this.hiveMQExtensions = hiveMQExtensions;
        this.pluginTaskExecutorService = pluginTaskExecutorService;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, @NotNull final Object msg) throws Exception {
        if (!(msg instanceof UNSUBSCRIBE)) {
            ctx.fireChannelRead(msg);
            return;
        }

        handleInboundUnsubscribe(ctx, (UNSUBSCRIBE) msg);

    }

    private void handleInboundUnsubscribe(
            @NotNull final ChannelHandlerContext ctx, @NotNull final UNSUBSCRIBE unsubscribe) throws Exception {
        final Channel channel = ctx.channel();

        final String clientId = channel.attr(ChannelAttributes.CLIENT_ID).get();
        if (clientId == null) {
            return;
        }
        final ClientContextImpl clientContext = channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).get();
        if (clientContext == null) {
            ctx.fireChannelRead(unsubscribe);
            return;
        }
        final List<UnsubscribeInboundInterceptor> unsubscribeInboundInterceptors =
                clientContext.getUnsubscribeInboundInterceptors();
        if (unsubscribeInboundInterceptors.isEmpty()) {
            ctx.fireChannelRead(unsubscribe);
            return;
        }

        final UnsubscribeInboundInputImpl input =
                new UnsubscribeInboundInputImpl(new UnsubscribePacketImpl(unsubscribe), clientId, channel);

        final UnsubscribeInboundOutputImpl output =
                new UnsubscribeInboundOutputImpl(asyncer, configurationService, unsubscribe);

        final UnsubscribeInboundInterceptorContext interceptorContext =
                new UnsubscribeInboundInterceptorContext(
                        UnsubscribeInboundInterceptorTask.class, clientId, input, ctx,
                        unsubscribeInboundInterceptors.size());

        for (final UnsubscribeInboundInterceptor interceptor : unsubscribeInboundInterceptors) {

            final HiveMQExtension extension = hiveMQExtensions.getExtensionForClassloader(
                    (IsolatedPluginClassloader) interceptor.getClass().getClassLoader());
            if (extension == null) {
                interceptorContext.increment(output);
                continue;
            }

            final UnsubscribeInboundInterceptorTask interceptorTask =
                    new UnsubscribeInboundInterceptorTask(interceptor, extension.getId());
            pluginTaskExecutorService.handlePluginInOutTaskExecution(
                    interceptorContext, input, output, interceptorTask);
        }
    }

    private static class UnsubscribeInboundInterceptorContext
            extends PluginInOutTaskContext<UnsubscribeInboundOutputImpl> {

        private final @NotNull UnsubscribeInboundInputImpl input;
        private final @NotNull ChannelHandlerContext ctx;
        private final int interceptorCount;
        private final @NotNull AtomicInteger counter;

        UnsubscribeInboundInterceptorContext(
                final @NotNull Class<?> taskClazz,
                final @NotNull String identifier,
                final @NotNull UnsubscribeInboundInputImpl input,
                final @NotNull ChannelHandlerContext ctx,
                final int interceptorCount) {
            super(taskClazz, identifier);
            this.input = input;
            this.ctx = ctx;
            this.interceptorCount = interceptorCount;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void pluginPost(final @NotNull UnsubscribeInboundOutputImpl output) {
            if (output.isTimedOut()) {
                log.debug("Async timeout on inbound UNSUBSCRIBE interception.");
                output.update(input.getUnsubscribePacket());
            } else if (output.getUnsubscribePacket().isModified()) {
                input.updateUnsubscribe(output.getUnsubscribePacket());
            }
            increment(output);
        }

        public void increment(final @NotNull UnsubscribeInboundOutputImpl output) {
            if (counter.incrementAndGet() == interceptorCount) {
                final UNSUBSCRIBE finalDisconnect = UNSUBSCRIBE.createUnsubscribeFrom(output.getUnsubscribePacket());
                ctx.fireChannelRead(finalDisconnect);
            }
        }
    }

    private static class UnsubscribeInboundInterceptorTask
            implements PluginInOutTask<UnsubscribeInboundInputImpl, UnsubscribeInboundOutputImpl> {

        private final @NotNull UnsubscribeInboundInterceptor interceptor;
        private final @NotNull String extensionId;

        UnsubscribeInboundInterceptorTask(
                @NotNull final UnsubscribeInboundInterceptor interceptor,
                @NotNull final String extensionId) {
            this.interceptor = interceptor;
            this.extensionId = extensionId;
        }

        @NotNull
        @Override
        public UnsubscribeInboundOutputImpl apply(
                final @NotNull UnsubscribeInboundInputImpl input,
                final @NotNull UnsubscribeInboundOutputImpl output) {
            try {
                interceptor.onInboundUnsubscribe(input, output);
            } catch (final Throwable e) {
                log.debug(
                        "Uncaught exception was thrown from extension with id \"{}\" on inbound unsubscribe request interception." +
                                "Extensions are responsible for their own exception handling.", extensionId);
                log.debug("Original Exception:" + e);
                output.update(input.getUnsubscribePacket());
            }
            return output;
        }

        @Override
        public @NotNull ClassLoader getPluginClassLoader() {
            return interceptor.getClass().getClassLoader();
        }
    }

}
