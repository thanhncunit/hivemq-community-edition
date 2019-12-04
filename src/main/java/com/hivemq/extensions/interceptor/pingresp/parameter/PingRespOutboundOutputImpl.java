package com.hivemq.extensions.interceptor.pingresp.parameter;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.pingresp.parameter.PingRespOutboundOutput;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.task.AbstractSimpleAsyncOutput;
import com.hivemq.extensions.executor.task.PluginTaskOutput;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class PingRespOutboundOutputImpl extends AbstractSimpleAsyncOutput<PingRespOutboundOutput>
        implements PingRespOutboundOutput, PluginTaskOutput, Supplier<PingRespOutboundOutputImpl> {

    public PingRespOutboundOutputImpl(final @NotNull PluginOutPutAsyncer asyncer) {
        super(asyncer);
    }

    @Override
    public @NotNull PingRespOutboundOutputImpl get() {
        return this;
    }
}
