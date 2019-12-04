package com.hivemq.extensions.interceptor.pingreq.parameter;

import com.hivemq.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.pingreq.parameter.PingReqInboundOutput;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.task.AbstractSimpleAsyncOutput;
import com.hivemq.extensions.executor.task.PluginTaskOutput;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class PingReqInboundOutputImpl extends AbstractSimpleAsyncOutput<PingReqInboundOutput>
        implements PingReqInboundOutput, PluginTaskOutput, Supplier<PingReqInboundOutputImpl> {


    public PingReqInboundOutputImpl(final @NotNull PluginOutPutAsyncer asyncer) {
        super(asyncer);
    }

    @Override
    public @NotNull PingReqInboundOutputImpl get() {
        return this;
    }


}