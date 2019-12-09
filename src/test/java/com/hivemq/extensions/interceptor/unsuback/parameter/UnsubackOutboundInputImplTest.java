package com.hivemq.extensions.interceptor.unsuback.parameter;

import com.hivemq.mqtt.message.ProtocolVersion;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;
import util.TestMessageUtil;

public class UnsubackOutboundInputImplTest {

    @Test
    public void test_construction() {
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.attr(ChannelAttributes.MQTT_VERSION).set(ProtocolVersion.MQTTv5);

        final UNSUBACK unsuback = TestMessageUtil.createFullMqtt5Unsuback();
        final UnsubackOutboundInputImpl input = new UnsubackOutboundInputImpl("client", embeddedChannel, unsuback);

        Assert.assertNotNull(input.getClientInformation());
        Assert.assertNotNull(input.getConnectionInformation());
        Assert.assertNotNull(input.getUnsubackPacket());
    }

    @Test(expected = NullPointerException.class)
    public void test_clientId_null() {
        final UNSUBACK unsubackPacket = TestMessageUtil.createFullMqtt5Unsuback();
        new UnsubackOutboundInputImpl(null, new EmbeddedChannel(), unsubackPacket);
    }

    @Test(expected = NullPointerException.class)
    public void test_channel_null() {
        final UNSUBACK unsubackPacket = TestMessageUtil.createFullMqtt5Unsuback();
        new UnsubackOutboundInputImpl("client", null, unsubackPacket);
    }

    @Test(expected = NullPointerException.class)
    public void test_packet_null() {
        new UnsubackOutboundInputImpl(null, new EmbeddedChannel(), null);
    }

}