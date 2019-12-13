package com.hivemq.extension.sdk.api.packets.unsuback;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;

import java.util.List;
import java.util.Optional;

/**
 * Represents a UNSUBACK packet.
 * <p>
 * Contains all values of an MQTT 5 UNSUBACK, but will also be used to represent MQTT 3 UNSUBACK messages.
 *
 * @author Robin Atherton
 * @author Silvio Giebl
 */
@Immutable
@DoNotImplement
public interface UnsubackPacket {

    /**
     * Represents the return codes for the QoS levels of the different Topics contained in the corresponding UNSUBSCRIBE
     * message as well as potetial failure codes.
     *
     * @return The reason codes for the unsubscribed topics.
     */
    @Immutable @NotNull List<@NotNull UnsubackReasonCode> getReasonCodes();

    /**
     * @return The reason codes as a String.
     */
    @NotNull Optional<String> getReasonString();

    /**
     * The packet identifier of the UNSUBACK packet.
     *
     * @return The packet identifier.
     */
    int getPacketIdentifier();

    /**
     * The user properties from the UNSUBACK packet.
     *
     * @return The {@link UserProperties} of the UNSUBACK packet.
     */
    @Immutable @NotNull UserProperties getUserProperties();
}
