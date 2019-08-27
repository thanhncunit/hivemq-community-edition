package com.hivemq.mqtt.message.unsubscribe;

import com.hivemq.extensions.packets.unsubscribe.UnsubscribePacketImpl;
import org.junit.Test;
import util.TestMessageUtil;

import static org.junit.Assert.assertEquals;

public class UNSUBSCRIBETest {

    @Test
    public void test_construction() {
        final UNSUBSCRIBE unsubscribe = TestMessageUtil.createFullMqtt5Unsubscribe();
        final UnsubscribePacketImpl packet = new UnsubscribePacketImpl(unsubscribe);
        final UNSUBSCRIBE unsubscribeFrom = UNSUBSCRIBE.createUnsubscribeFrom(packet);

        assertEquals(unsubscribe.getTopics(), unsubscribeFrom.getTopics());
        assertEquals(unsubscribe.getPacketIdentifier(), unsubscribeFrom.getPacketIdentifier());
        assertEquals(unsubscribe.getEncodedLength(), unsubscribeFrom.getEncodedLength());
    }

}