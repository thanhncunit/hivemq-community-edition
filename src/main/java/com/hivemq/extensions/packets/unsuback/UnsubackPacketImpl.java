/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.packets.unsuback;

import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackPacket;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackReasonCode;
import com.hivemq.mqtt.message.reason.Mqtt5UnsubAckReasonCode;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;

import java.util.List;

/**
 * @author Robin Atherton
 */
public class UnsubackPacketImpl implements UnsubackPacket {

    private final @NotNull UNSUBACK unsuback;
    private final @NotNull ImmutableList<UnsubackReasonCode> unsubackReasonCodes;
    private final @NotNull UserProperties userProperties;
    private final @NotNull String reasonString;

    public UnsubackPacketImpl(@NotNull final UNSUBACK unsuback) {
        this.unsuback = unsuback;
        final ImmutableList.Builder<UnsubackReasonCode> builder = ImmutableList.builder();
        for (final Mqtt5UnsubAckReasonCode code : this.unsuback.getReasonCodes()) {
            builder.add(UnsubackReasonCode.valueOf(code.name()));
        }
        this.unsubackReasonCodes = builder.build();
        this.userProperties = this.unsuback.getUserProperties().getPluginUserProperties();
        this.reasonString = this.unsuback.getReasonString();
    }

    public UnsubackPacketImpl(@NotNull final UnsubackPacket unsubackPacket) {
        this.unsuback = UNSUBACK.createUnsubackFrom(unsubackPacket);
        final ImmutableList.Builder<UnsubackReasonCode> builder = ImmutableList.builder();
        for (final Mqtt5UnsubAckReasonCode code : this.unsuback.getReasonCodes()) {
            builder.add(UnsubackReasonCode.valueOf(code.name()));
        }
        this.unsubackReasonCodes = builder.build();
        this.userProperties = this.unsuback.getUserProperties().getPluginUserProperties();
        this.reasonString = this.unsuback.getReasonString();
    }

    @Override
    public @NotNull List<UnsubackReasonCode> getReasonCodes() {
        return unsubackReasonCodes;
    }

    @Override
    public @NotNull String getReasonString() {
        return this.reasonString;
    }

    @Override
    public int getPacketIdentifier() {
        return unsuback.getPacketIdentifier();
    }

    @Override
    public @NotNull UserProperties getUserProperties() {
        return userProperties;
    }
}
