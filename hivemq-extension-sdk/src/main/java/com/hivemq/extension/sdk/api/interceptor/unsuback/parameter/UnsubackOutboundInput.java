package com.hivemq.extension.sdk.api.interceptor.unsuback.parameter;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.unsuback.UnsubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackPacket;
import com.hivemq.extension.sdk.api.parameter.ClientBasedInput;

/**
 * This i s the input parameter of any {@link UnsubackOutboundInterceptor} providing UNSUBACK, connection and client
 * based information.
 *
 * @author Robin Atherton
 */
public interface UnsubackOutboundInput extends ClientBasedInput {

    /**
     * The unmodifiable UNSUBACK packet that was intercepted.
     *
     * @return A unmodifiable {@link UnsubackPacket}
     */
    @NotNull
    @Immutable UnsubackPacket getUnsubackPacket();
}
