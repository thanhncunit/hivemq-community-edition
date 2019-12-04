package com.hivemq.extension.sdk.api.packets.unsuback;

/**
 * @author Robin Atherton
 */
public enum UnsubackReasonCode {

    /**
     * The subscription is deleted.
     */
    SUCCESS,

    /**
     * No matching Topic Filter is being used by the Client.
     */
    NO_SUBSCRIPTIONS_EXISTED,

    /**
     * The unsubscribe could not be completed and the Server does not wish to reveal the reason or none of the other
     * Reason Codes apply.
     */
    UNSPECIFIED_ERROR,

    /**
     * The UNSUBSCRIBE is valid but the Server does not accept it.
     */
    IMPLEMENTATION_SPECIFIC_ERROR,

    /**
     * The Client is not authorized to unsubscribe.
     */
    NOT_AUTHORIZED,

    /**
     * The Topic Filter is correctly formed but is not allowed for this Client.
     */
    TOPIC_FILTER_INVALID,

    /**
     * The specified Packet Identfier is already in use.
     */
    PACKET_IDENTIFIER_IN_USE
}
