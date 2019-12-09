package com.hivemq.extensions.packets.unsuback;

import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackReasonCode;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserPropertiesBuilder;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import com.hivemq.mqtt.message.reason.Mqtt5UnsubAckReasonCode;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;
import org.junit.Before;
import org.junit.Test;
import util.TestConfigurationBootstrap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModifiableUnsubackPacketImplTest {

    private ModifiableUnsubackPacketImpl packet;

    private UNSUBACK original;

    private FullConfigurationService configurationService;
    private List<Mqtt5UnsubAckReasonCode> originalreasonCodes;
    private List<UnsubackReasonCode> modifiedReasonCodes;

    @Before
    public void setUp() throws Exception {
        originalreasonCodes = new ArrayList<>();
        originalreasonCodes.add(Mqtt5UnsubAckReasonCode.PACKET_IDENTIFIER_IN_USE);
        originalreasonCodes.add(Mqtt5UnsubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        originalreasonCodes.add(Mqtt5UnsubAckReasonCode.TOPIC_FILTER_INVALID);
        original = createTestUnsuback(1, originalreasonCodes, "reasonCodes");
        packet = createTestUnsubackPacket(1, originalreasonCodes, "reasonCodes");

        modifiedReasonCodes = new ArrayList<>();
        modifiedReasonCodes.add(UnsubackReasonCode.PACKET_IDENTIFIER_IN_USE);
        modifiedReasonCodes.add(UnsubackReasonCode.NO_SUBSCRIPTIONS_EXISTED);
        modifiedReasonCodes.add(UnsubackReasonCode.TOPIC_FILTER_INVALID);
    }

    @Test
    public void test_change_all_valid_values() {
        final List<UnsubackReasonCode> reasonCodes = new ArrayList<>();
        reasonCodes.add(UnsubackReasonCode.PACKET_IDENTIFIER_IN_USE);
        reasonCodes.add(UnsubackReasonCode.SUCCESS);
        reasonCodes.add(UnsubackReasonCode.NOT_AUTHORIZED);

        packet.setReasonString("testReasonString");
        packet.setReasonCodes(reasonCodes);

        assertEquals("testReasonString", packet.getReasonString());
        assertEquals(UnsubackReasonCode.PACKET_IDENTIFIER_IN_USE, packet.getReasonCodes().get(0));
        assertEquals(UnsubackReasonCode.SUCCESS, packet.getReasonCodes().get(1));
        assertEquals(UnsubackReasonCode.NOT_AUTHORIZED, packet.getReasonCodes().get(2));
    }

    @Test
    public void test_modify_packet() {
        packet = new ModifiableUnsubackPacketImpl(configurationService, original);
        packet.setReasonCodes(modifiedReasonCodes);
        assertTrue(packet.isModified());

        packet = new ModifiableUnsubackPacketImpl(configurationService, original);
        packet.setReasonString("testTestTest");
        assertTrue(packet.isModified());
    }

    @Test(expected = NullPointerException.class)
    public void test_set_reason_string_null() {
        packet.setReasonString(null);
    }

    @Test(expected = NullPointerException.class)
    public void test_set_reason_codes_null() {
        packet.setReasonString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reasonString_invalid_input() {
        packet.setReasonString("topic" + '\u0001');
    }

    @Test(expected = IllegalArgumentException.class)
    public void reasonString_exceeds_max_length() {
        final StringBuilder s = new StringBuilder("s");
        for (int i = 0; i < 65535; i++) {
            s.append("s");
        }
        packet.setReasonString(s.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reason_codes_with_different_sizes() {
        final ArrayList<UnsubackReasonCode> subackReasonCodes = new ArrayList<>();
        subackReasonCodes.add(UnsubackReasonCode.TOPIC_FILTER_INVALID);
        subackReasonCodes.add(UnsubackReasonCode.PACKET_IDENTIFIER_IN_USE);
        packet.setReasonCodes(subackReasonCodes);
    }

    private @NotNull ModifiableUnsubackPacketImpl createTestUnsubackPacket(
            final int packetIdentifier,
            final @NotNull List<Mqtt5UnsubAckReasonCode> reasonCodes,
            final @NotNull String reasonString) {
        configurationService = new TestConfigurationBootstrap().getFullConfigurationService();
        final Mqtt5UserPropertiesBuilder builder =
                Mqtt5UserProperties.builder().add(new MqttUserProperty("test", "test"));
        final Mqtt5UserProperties properties = builder.build();
        final UNSUBACK unsuback = new UNSUBACK(packetIdentifier, reasonCodes, reasonString, properties);
        return new ModifiableUnsubackPacketImpl(configurationService, unsuback);
    }

    private @NotNull UNSUBACK createTestUnsuback(
            final int packetIdentifier,
            final @NotNull List<Mqtt5UnsubAckReasonCode> reasonCodes,
            final @NotNull String reasonString) {
        final Mqtt5UserPropertiesBuilder builder =
                Mqtt5UserProperties.builder().add(new MqttUserProperty("test", "test"));
        final Mqtt5UserProperties properties = builder.build();
        return new UNSUBACK(packetIdentifier, reasonCodes, reasonString, properties);

    }
}