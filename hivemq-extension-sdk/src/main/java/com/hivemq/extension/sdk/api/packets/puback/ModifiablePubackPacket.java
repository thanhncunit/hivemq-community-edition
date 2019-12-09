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
package com.hivemq.extension.sdk.api.packets.puback;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.publish.AckReasonCode;

/**
 * A {@link PubackPacket} that can be modified before it is sent to the client.
 *
 * @author Yannick Weber
 */
@DoNotImplement
public interface ModifiablePubackPacket extends PubackPacket {

    /**
     * Set a {@link AckReasonCode} to the PUBACK packet.
     * <p>
     * Switching from successful to unsuccessful and vice versa is not supported, in that case an {@link
     * IllegalStateException} is thrown.
     *
     * @param reasonCode The reason code to set.
     * @throws NullPointerException  If reason code is <null>.
     * @throws IllegalStateException If switching from successful reason code to unsuccessful reason code or vice
     *                               versa.
     * @see AckReasonCode How reason codes are translated from MQTT 5 to MQTT 3.
     */
    void setReasonCode(@NotNull AckReasonCode reasonCode);

    /**
     * Set the reason string.
     * <p>
     * A reason must not be set for a successful publish.
     * <p>
     * This setting is only respected for MQTT 5 clients. For MQTT 3.x clients this setting is ignored.
     *
     * @param reasonString The reason string to set.
     * @throws IllegalStateException    If reason code is {@link AckReasonCode#SUCCESS}.
     * @throws IllegalArgumentException If the reason string is not a valid UTF-8 string.
     * @throws IllegalArgumentException If the reason string exceeds the UTF-8 string length limit.
     */
    void setReasonString(@Nullable String reasonString);

    /**
     * Get the modifiable {@link UserProperties} of the PUBACK packet.
     *
     * @return Modifiable user properties.
     */
    @Override
    @NotNull ModifiableUserProperties getUserProperties();
}
