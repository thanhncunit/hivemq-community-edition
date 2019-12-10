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
package com.hivemq.extension.sdk.api.packets.unsuback;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;

import java.util.List;

/**
 * @author Robin Atherton
 */
@DoNotImplement
public interface ModifiableUnsubackPacket extends UnsubackPacket {

    /**
     * Sets a list of {@link SubackReasonCode}s for the UNSUBACK packet.
     *
     * @param reasonCodes the list of reason codes to be written into the UNSUBACK packet.
     * @throws NullPointerException     If the list of reason codes is <null>.
     * @throws IllegalArgumentException If the amount of reason codes passed differs from that contained in the packet
     *                                  being manipulated.
     */
    void setReasonCodes(final @NotNull List<UnsubackReasonCode> reasonCodes);

    /**
     * Sets the reason string for the SUBACK packet.
     *
     * @param reasonString the reason to be set as a String.
     * @throws NullPointerException     If reason String is <null>.
     * @throws IllegalArgumentException If the reason string is not a valid UTF-8 string.
     * @throws IllegalArgumentException If the reason string exceeds the UTF-8 string length limit.
     */
    void setReasonString(final @NotNull String reasonString);

    /**
     * Gets the modifiable {@link UserProperties} of the SUBACK packet.
     *
     * @return Modifiable user properties.
     */
    @Override
    @NotNull ModifiableUserProperties getUserProperties();

}