package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.packets.suback.ModifiableSubackPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;
import util.TestMessageUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Robin Atherton
 */
public class SubackOutboundOutputImplTest {

    private SUBACK subAck;

    @Mock
    private PluginOutPutAsyncer asyncer;
    private SubackOutboundOutputImpl output;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        final FullConfigurationService configurationService =
                new TestConfigurationBootstrap().getFullConfigurationService();
        subAck = TestMessageUtil.createFullMqtt5Suback();
        output = new SubackOutboundOutputImpl(configurationService, asyncer, subAck);
    }

    @Test
    public void test_getModifiable() {
        final ModifiableSubackPacket modifiableSubAckPacket = output.get().getSubackPacket();
        assertEquals(subAck.getPacketIdentifier(), modifiableSubAckPacket.getPacketIdentifier());
        assertEquals(
                subAck.getUserProperties().size(), modifiableSubAckPacket.getUserProperties().asList().size());
        assertEquals(subAck.getReasonString(), modifiableSubAckPacket.getReasonString());
        final List<SubackReasonCode> reasonCodes = modifiableSubAckPacket.getReasonCodes();
        for (int i = 0; i < reasonCodes.size(); i++) {
            assertEquals(subAck.getReasonCodes().get(i), Mqtt5SubAckReasonCode.valueOf(reasonCodes.get(i).name()));
        }
    }

}