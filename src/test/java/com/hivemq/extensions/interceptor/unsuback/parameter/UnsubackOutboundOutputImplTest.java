package com.hivemq.extensions.interceptor.unsuback.parameter;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.packets.unsuback.ModifiableUnsubackPacket;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.mqtt.message.reason.Mqtt5UnsubAckReasonCode;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;
import util.TestMessageUtil;

import static org.junit.Assert.assertEquals;

public class UnsubackOutboundOutputImplTest {

    private UNSUBACK unsuback;

    @Mock
    private PluginOutPutAsyncer asyncer;
    private UnsubackOutboundOutputImpl output;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        final FullConfigurationService configurationService =
                new TestConfigurationBootstrap().getFullConfigurationService();
        unsuback = TestMessageUtil.createFullMqtt5Unsuback();
        output = new UnsubackOutboundOutputImpl(configurationService, asyncer, unsuback);
    }

    @Test
    public void test_getModifiable() {
        final ModifiableUnsubackPacket modifiableUnsubackPacket = output.getUnsubackPacket();
        int i = 0;
        for (Mqtt5UnsubAckReasonCode reasonCode : unsuback.getReasonCodes()) {
            assertEquals(reasonCode.name(), unsuback.getReasonCodes().get(i).name());
            i++;
        }
        assertEquals(unsuback.getUserProperties().size(), modifiableUnsubackPacket.getUserProperties().asList().size());
    }
}