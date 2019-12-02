package com.hivemq.extension.sdk.api.packets.unsuback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;

import java.util.List;

/**
 * Represents a UNSUBACK packet.
 * <p>
 * Contains all values of an MQTT 5 UNSUBACK, but will also be used to represent MQTT 3 UNSUBACK messages.
 *
 * @author Robin Atherton
 */
public interface UnsubackPacket {

    /**
     * Represents the return codes for the QoS levels of the different Topics contained in the corresponding UNSUBSCRIBE
     * message as well as potetial failure codes.
     *
     * @return The reason codes for the unsubscribed topics.
     */
    @NotNull List<UnsubackReasonCode> getReasonCodes();

    /**
     * @return The reason codes as a String.
     */
    @NotNull String getReasonString();

    /**
     * The packet identifier of the UNSUBACK packet.
     *
     * @return The packet identifier.
     * @since 4.3
     */
    int getPacketIdentifier();

    /**
     * The user properties from the UNSUBACK packet.
     *
     * @return The {@link UserProperties} of the UNSUBACK packet.
     * @since 4.3
     */
    @NotNull UserProperties getUserProperties();
}
