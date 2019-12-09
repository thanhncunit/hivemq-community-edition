package com.hivemq.extension.sdk.api.packets.suback;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;

import java.util.List;
import java.util.Optional;

/**
 * Represents a SUBACK packet.
 * <p>
 * Contains all values of an MQTT 5 SUBACK, but will also be used to represent MQTT 3 SUBACK messages.
 *
 * @author Robin Atherton
 */
@Immutable
@DoNotImplement
public interface SubackPacket {

    /**
     * The reason codes for each subscription in the corresponding SUBSCRIBE message.
     *
     * @return The reason codes for the subscriptions.
     */
    @Immutable @NotNull List<@NotNull SubackReasonCode> getReasonCodes();

    /**
     * The optional reason string of this SUBACK packet.
     *
     * @return The optional reason string.
     */
    @NotNull Optional<String> getReasonString();

    /**
     * The packet identifier of this SUBACK packet.
     *
     * @return The packet identifier.
     */
    int getPacketIdentifier();

    /**
     * The {@link UserProperties} of this SUBACK packet.
     *
     * @return The user properties.
     */
    @Immutable @NotNull UserProperties getUserProperties();
}
