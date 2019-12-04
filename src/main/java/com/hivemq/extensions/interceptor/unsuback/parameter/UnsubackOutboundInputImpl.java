package com.hivemq.extensions.interceptor.unsuback.parameter;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionInformation;
import com.hivemq.extension.sdk.api.interceptor.unsuback.parameter.UnsubackOutboundInput;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackPacket;
import com.hivemq.extensions.PluginInformationUtil;
import com.hivemq.extensions.executor.task.PluginTaskInput;
import com.hivemq.extensions.packets.unsuback.UnsubackPacketImpl;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;
import io.netty.channel.Channel;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class UnsubackOutboundInputImpl implements Supplier<UnsubackOutboundInputImpl>, UnsubackOutboundInput,
        PluginTaskInput {

    private final @NotNull ConnectionInformation connectionInformation;
    private final @NotNull ClientInformation clientInformation;
    private @NotNull UnsubackPacket unsubackPacket;

    public UnsubackOutboundInputImpl(
            final @NotNull String clientId,
            final @NotNull Channel channel,
            final @NotNull UNSUBACK unsuback) {
        this.clientInformation = PluginInformationUtil.getAndSetClientInformation(channel, clientId);
        this.connectionInformation = PluginInformationUtil.getAndSetConnectionInformation(channel);
        this.unsubackPacket = new UnsubackPacketImpl(unsuback);
    }

    @Override
    public @NotNull @Immutable UnsubackPacket getUnsubackPacket() {
        return unsubackPacket;
    }

    @Override
    public @NotNull ConnectionInformation getConnectionInformation() {
        return connectionInformation;
    }

    @Override
    public @NotNull ClientInformation getClientInformation() {
        return clientInformation;
    }

    @NotNull
    @Override
    public UnsubackOutboundInputImpl get() {
        return this;
    }

    public void update(final @NotNull UnsubackPacket unsubackPacket) {
        this.unsubackPacket = new UnsubackPacketImpl(unsubackPacket);
    }
}
