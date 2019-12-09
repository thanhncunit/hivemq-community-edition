package com.hivemq.extensions.packets.unsuback;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
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
            throw new IllegalArgumentException("You cannot change the amount of reason codes.");
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
