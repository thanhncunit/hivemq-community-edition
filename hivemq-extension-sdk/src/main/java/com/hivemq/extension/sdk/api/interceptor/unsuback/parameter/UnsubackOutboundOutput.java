package com.hivemq.extension.sdk.api.interceptor.unsuback.parameter;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.async.Async;
import com.hivemq.extension.sdk.api.async.SimpleAsyncOutput;
import com.hivemq.extension.sdk.api.packets.unsuback.ModifiableUnsubackPacket;

import java.time.Duration;

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
    ModifiableUnsubackPacket getSubackPacket();

    /**
     * If the timeout is expired before {@link Async#resume()} is called then the outcome is handled as failed. This
     * means that the outcome results an unmodified SUBACK is sent to the server.
     * <p>
     * Do not call this method more than once. If an async method is called multiple times an exception is thrown.
     *
     * @param timeout Timeout that HiveMQ waits for the result of the async operation.
     * @throws UnsupportedOperationException If async is called more than once.
     * @since 4.3.0
     */
    @Override
    @NotNull Async<UnsubackOutboundOutput> async(@NotNull Duration timeout);

}
