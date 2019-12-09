/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extension.sdk.api.interceptor.pubcomp;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.async.TimeoutFallback;
import com.hivemq.extension.sdk.api.interceptor.Interceptor;
import com.hivemq.extension.sdk.api.interceptor.pubcomp.parameter.PubcompOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.pubcomp.parameter.PubcompOutboundOutput;

import java.time.Duration;

/**
 * Interface for the outbound PUBCOMP interception.
 * <p>
 * Interceptors are always called by the same Thread for all messages from the same client.
 * <p>
 * If the same instance is shared between multiple clients it can be called in different Threads and must therefore be
 * thread-safe.
 * <p>
 * When the method {@link #onOutboundPubcomp(PubcompOutboundInput, PubcompOutboundOutput)} throws an exception or a call
 * to {@link PubcompOutboundOutput#async(Duration)} times out with {@link TimeoutFallback#FAILURE}, the exception will
 * be logged and the PUBCOMP will be sent to the client without any changes.
 *
 * @author Yannick Weber
 */
@FunctionalInterface
public interface PubcompOutboundInterceptor extends Interceptor {

    /**
     * When a {@link PubcompOutboundInterceptor} is set through any extension, this method gets called for every
     * outgoing PUBCOMP packet for any MQTT client.
     *
     * @param pubcompOutboundInput  The {@link PubcompOutboundInput} parameter.
     * @param pubcompOutboundOutput The {@link PubcompOutboundOutput} parameter.
     */
    void onOutboundPubcomp(
            @NotNull PubcompOutboundInput pubcompOutboundInput,
            @NotNull PubcompOutboundOutput pubcompOutboundOutput);
}
