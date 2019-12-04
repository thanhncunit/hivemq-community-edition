package com.hivemq.extensions.interceptor.unsuback.parameter;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.unsuback.parameter.UnsubackOutboundOutput;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackPacket;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.task.AbstractSimpleAsyncOutput;
import com.hivemq.extensions.packets.unsuback.ModifiableUnsubackPacketImpl;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class UnsubackOutboundOutputImpl extends AbstractSimpleAsyncOutput<UnsubackOutboundOutput> implements
        UnsubackOutboundOutput, Supplier<UnsubackOutboundOutputImpl> {

    private final @NotNull FullConfigurationService configurationService;
    private @NotNull ModifiableUnsubackPacketImpl unsubackPacket;

    public UnsubackOutboundOutputImpl(
            final @NotNull FullConfigurationService configurationService,
            final @NotNull PluginOutPutAsyncer asyncer,
            final @NotNull UNSUBACK unsuback) {
        super(asyncer);
        this.configurationService = configurationService;
        this.unsubackPacket = new ModifiableUnsubackPacketImpl(configurationService, unsuback);
    }

    @Override
    public @NotNull ModifiableUnsubackPacketImpl getUnsubackPacket() {
        return this.unsubackPacket;
    }

    @Override
    public @NotNull UnsubackOutboundOutputImpl get() {
        return this;
    }

    public void update(final @NotNull UnsubackPacket unsubackPacket) {
        this.unsubackPacket = new ModifiableUnsubackPacketImpl(configurationService, unsubackPacket);
    }

}
