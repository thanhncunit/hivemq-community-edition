package com.hivemq.extension.sdk.api.interceptor.unsuback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.Interceptor;
import com.hivemq.extension.sdk.api.interceptor.unsuback.parameter.UnsubackOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.unsuback.parameter.UnsubackOutboundOutput;

/**
 * Interface for the UNSUBACK outbound interception.
 * <p>
 * Interceptors are always called by the same Thread for all UNSUBACK messages from the same client.
 * <p>
 * If the same instance is shared between multiple clients it can be called in different Threads and must therefore be
 * thread-safe.
 *
 * @author Robin Atherton
 */
public interface UnsubackOutboundInterceptor extends Interceptor {

    /**
     * When a {@link UnsubackOutboundInterceptor} is set through any extension, this method gets called for every
     * outbound UNSUBACK packet from any MQTT client.
     * <p>
     * When the extension is enabled after HiveMQ is already running this method will also be called for future UNSUBACK
     * of clients that are already connected.
     *
     * @param unsubackOutboundInput  The {@link UnsubackOutboundInput} parameter.
     * @param unsubackOutboundOutput The {@link UnsubackOutboundOutput} parameter.
     */
    void onOutboundUnsuback(
            @NotNull UnsubackOutboundInput unsubackOutboundInput,
            @NotNull UnsubackOutboundOutput unsubackOutboundOutput);
}
