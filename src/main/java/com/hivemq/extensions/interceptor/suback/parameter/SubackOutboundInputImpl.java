package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionInformation;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundInput;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extensions.PluginInformationUtil;
import com.hivemq.extensions.executor.task.PluginTaskInput;
import com.hivemq.extensions.packets.suback.SubackPacketImpl;
import com.hivemq.mqtt.message.suback.SUBACK;
import io.netty.channel.Channel;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class SubackOutboundInputImpl
        implements Supplier<SubackOutboundInputImpl>, SubackOutboundInput, PluginTaskInput {

    private final @NotNull ClientInformation clientInformation;
    private final @NotNull ConnectionInformation connectionInformation;
    private @NotNull SubackPacketImpl subackPacket;

    public SubackOutboundInputImpl(
            final @NotNull String clientId,
            final @NotNull Channel channel,
            final @NotNull SUBACK suback) {

        clientInformation = PluginInformationUtil.getAndSetClientInformation(channel, clientId);
        connectionInformation = PluginInformationUtil.getAndSetConnectionInformation(channel);
        subackPacket = new SubackPacketImpl(suback);
    }

    @Override
    public @NotNull ClientInformation getClientInformation() {
        return clientInformation;
    }

    @Override
    public @NotNull ConnectionInformation getConnectionInformation() {
        return connectionInformation;
    }

    @Override
    public @Immutable @NotNull SubackPacket getSubackPacket() {
        return subackPacket;
    }

    @Override
    public @NotNull SubackOutboundInputImpl get() {
        return this;
    }

    public void update(final @NotNull SubackPacket subAckPacket) {
        this.subackPacket = new SubackPacketImpl(subAckPacket);
    }
}
