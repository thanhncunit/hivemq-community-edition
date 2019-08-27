package com.hivemq.extensions.interceptor.unsubscribe.parameter;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.unsubscribe.ModifiableUnsubscribePacket;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.mqtt.message.unsubscribe.UNSUBSCRIBE;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;
import util.TestMessageUtil;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Robin Atherton
 */
public class UnsubscribeInboundOutputImplTest {

    @NotNull
    private FullConfigurationService config;
    @NotNull
    private UNSUBSCRIBE unsubscribe;

    @NotNull
    @Mock
    private PluginOutPutAsyncer pluginOutPutAsyncer;
    @NotNull
    private UnsubscribeInboundOutputImpl output;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        config = new TestConfigurationBootstrap().getFullConfigurationService();
        unsubscribe = TestMessageUtil.createFullMqtt5Unsubscribe();
        output = new UnsubscribeInboundOutputImpl(pluginOutPutAsyncer, config, unsubscribe);
        assertEquals(output, output.get());
    }

    @Test
    public void test_getModifiable() {
        final ModifiableUnsubscribePacket modifiableUnsubscribePacket = output.get().getUnsubscribePacket();
        final ArrayList<String> strings = new ArrayList<>();
        strings.add("Topic1");
        strings.add("Topics2");
        modifiableUnsubscribePacket.setTopics(strings);
        assertNotEquals(unsubscribe.getTopics(), modifiableUnsubscribePacket.getTopics());
    }

}
