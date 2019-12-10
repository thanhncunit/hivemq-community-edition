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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.unsuback.ModifiableUnsubackPacket;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackPacket;
import com.hivemq.extension.sdk.api.packets.unsuback.UnsubackReasonCode;
import com.hivemq.extensions.packets.general.InternalUserProperties;
import com.hivemq.extensions.packets.general.ModifiableUserPropertiesImpl;
import com.hivemq.extensions.services.builder.PluginBuilderUtil;
import com.hivemq.mqtt.message.reason.Mqtt5UnsubAckReasonCode;
import com.hivemq.mqtt.message.unsuback.UNSUBACK;

import java.util.List;
import java.util.Objects;

/**
 * @author Robin Atherton
 */
public class ModifiableUnsubackPacketImpl implements ModifiableUnsubackPacket {

    private final @NotNull FullConfigurationService configurationService;
    private final @NotNull ModifiableUserPropertiesImpl userProperties;
    private final int packetIdentifier;

    private boolean modified = false;
    private @NotNull String reasonString;
    private @NotNull ImmutableList<UnsubackReasonCode> reasonCodes;

    public ModifiableUnsubackPacketImpl(
            final @NotNull FullConfigurationService fullConfigurationService,
            final @NotNull UNSUBACK unsuback) {
        this.configurationService = fullConfigurationService;
        this.userProperties = new ModifiableUserPropertiesImpl(
                unsuback.getUserProperties().getPluginUserProperties(),
                fullConfigurationService.securityConfiguration().validateUTF8());
        this.packetIdentifier = unsuback.getPacketIdentifier();
        this.reasonString = unsuback.getReasonString();
        final ImmutableList.Builder<UnsubackReasonCode> builder = ImmutableList.builder();
        for (final Mqtt5UnsubAckReasonCode code : unsuback.getReasonCodes()) {
            builder.add(UnsubackReasonCode.valueOf(code.name()));
        }
        this.reasonCodes = builder.build();
    }

    public ModifiableUnsubackPacketImpl(
            final @NotNull FullConfigurationService fullConfigurationService,
            final @NotNull UnsubackPacket unsubackPacket) {
        this.configurationService = fullConfigurationService;
        this.userProperties = new ModifiableUserPropertiesImpl(
                (InternalUserProperties) unsubackPacket.getUserProperties(),
                fullConfigurationService.securityConfiguration().validateUTF8());
        this.packetIdentifier = unsubackPacket.getPacketIdentifier();
        this.reasonString = unsubackPacket.getReasonString();
        final ImmutableList.Builder<UnsubackReasonCode> builder = ImmutableList.builder();
        for (final UnsubackReasonCode code : unsubackPacket.getReasonCodes()) {
            builder.add(UnsubackReasonCode.valueOf(code.name()));
        }
        this.reasonCodes = builder.build();
    }

    @Override
    public void setReasonString(@NotNull final String reasonString) {
        Preconditions.checkNotNull(reasonString, "Reason String must never be null.");
        PluginBuilderUtil.checkReasonString(reasonString, configurationService.securityConfiguration().validateUTF8());
        if (reasonString.equals(this.reasonString)) {
            return;
        }
        this.reasonString = reasonString;
        this.modified = true;
    }

    @Override
    public void setReasonCodes(@NotNull final List<UnsubackReasonCode> reasonCodes) {
        Preconditions.checkNotNull(reasonCodes, "Reason codes must never be null");
        if (Objects.equals(this.reasonCodes, reasonCodes)) {
            return;
        }
        if (reasonCodes.size() != this.reasonCodes.size()) {
            throw new IllegalArgumentException("The amount of UNSUBACK reason codes cannot be changed.");
        }
        for (int i = 0; i < reasonCodes.size(); i++) {
            if (this.reasonCodes.get(i).equals(UnsubackReasonCode.SUCCESS)) {
                if (!reasonCodes.get(i).equals(UnsubackReasonCode.SUCCESS)) {
                    throw new IllegalArgumentException(
                            "Cannot change UNSUBACK reason code from successful to unsuccessful. This is caused by the reason code at: " +
                                    i);
                }
            }
            if (!this.reasonCodes.get(i).equals(UnsubackReasonCode.SUCCESS)) {
                if (reasonCodes.get(i).equals(UnsubackReasonCode.SUCCESS)) {
                    throw new IllegalArgumentException(
                            "Cannot change UNSUBACK reason code from unsuccessful to successful. This is caused by the reason code at: " +
                                    i);

                }
            }
        }
        this.reasonCodes = ImmutableList.copyOf(reasonCodes);
        this.modified = true;
    }

    @Override
    public @NotNull List<UnsubackReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @Override
    public @NotNull String getReasonString() {
        return this.reasonString;
    }

    @Override
    public int getPacketIdentifier() {
        return this.packetIdentifier;
    }

    @Override
    public @NotNull ModifiableUserProperties getUserProperties() {
        return this.userProperties;
    }

    public boolean isModified() {
        return modified;
    }
}
