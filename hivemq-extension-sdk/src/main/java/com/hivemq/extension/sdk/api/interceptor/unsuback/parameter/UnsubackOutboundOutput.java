package com.hivemq.extension.sdk.api.interceptor.unsuback.parameter;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.async.SimpleAsyncOutput;
import com.hivemq.extension.sdk.api.packets.unsuback.ModifiableUnsubackPacket;

/**
 * @author Robin Atherton
 */
public interface UnsubackOutboundOutput extends SimpleAsyncOutput<UnsubackOutboundOutput> {

    /**
     * Use this object to make any changes to the outbound SUBACK.
     *
     * @return A {@link ModifiableUnsubackPacket}
     * @since 4.3.0
     */
    @NotNull
    ModifiableUnsubackPacket getUnsubackPacket();

}
