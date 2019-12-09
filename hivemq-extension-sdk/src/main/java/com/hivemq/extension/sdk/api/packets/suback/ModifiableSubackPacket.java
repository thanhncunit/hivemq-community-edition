package com.hivemq.extension.sdk.api.packets.suback;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;

import java.util.List;

/**
 * Represents a SUBACK packet that can be modified.
 *
 * @author Robin Atherton
 */
@DoNotImplement
public interface ModifiableSubackPacket extends SubackPacket {

    /**
     * Sets the list of {@link SubackReasonCode reason codes} of the SUBACK packet.
     *
     * @param reasonCodes the list of reason codes.
     * @throws NullPointerException     If the list or an individual reason code is <code>null</code>.
     * @throws IllegalArgumentException If the amount of reason codes passed differs from that contained in the packet
     *                                  being manipulated.
     */
    void setReasonCodes(@NotNull List<@NotNull SubackReasonCode> reasonCodes);

    /**
     * Sets the reason string of the SUBACK packet.
     *
     * @param reasonString the reason string or <code>null</code> to remove the reason string.
     * @throws IllegalArgumentException If the reason string is not a valid UTF-8 string.
     * @throws IllegalArgumentException If the reason string exceeds the UTF-8 string length limit.
     */
    void setReasonString(@Nullable String reasonString);

    /**
     * The modifiable {@link UserProperties} of the SUBACK packet.
     *
     * @return Modifiable user properties.
     */
    @Override
    @NotNull ModifiableUserProperties getUserProperties();
}
