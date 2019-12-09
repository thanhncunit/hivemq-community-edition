package com.hivemq.extensions.packets.suback;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.suback.ModifiableSubackPacket;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.extensions.packets.general.InternalUserProperties;
import com.hivemq.extensions.packets.general.ModifiableUserPropertiesImpl;
import com.hivemq.extensions.services.builder.PluginBuilderUtil;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Robin Atherton
 */
public class ModifiableSubackPacketImpl implements ModifiableSubackPacket {

    private final @NotNull FullConfigurationService configurationService;
    private final @NotNull ModifiableUserPropertiesImpl userProperties;
    private final int packetIdentifier;

    private boolean modified = false;
    private @NotNull String reasonString;
    private @NotNull ImmutableList<SubackReasonCode> reasonCodes;

    public ModifiableSubackPacketImpl(
            final @NotNull FullConfigurationService fullConfigurationService,
            final @NotNull SUBACK subAck) {
        this.configurationService = fullConfigurationService;
        this.userProperties = new ModifiableUserPropertiesImpl(
                subAck.getUserProperties().getPluginUserProperties(),
                fullConfigurationService.securityConfiguration().validateUTF8());
        this.packetIdentifier = subAck.getPacketIdentifier();
        this.reasonString = subAck.getReasonString();
        final ArrayList<SubackReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final Mqtt5SubAckReasonCode code : subAck.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
        this.reasonCodes = ImmutableList.copyOf(subAckReasonCodes);
    }

    public ModifiableSubackPacketImpl(
            final @NotNull FullConfigurationService fullConfigurationService,
            final @NotNull SubackPacket subackPacket) {
        this.configurationService = fullConfigurationService;
        this.userProperties = new ModifiableUserPropertiesImpl(
                (InternalUserProperties) subackPacket.getUserProperties(),
                fullConfigurationService.securityConfiguration().validateUTF8());
        this.packetIdentifier = subackPacket.getPacketIdentifier();
        this.reasonString = subackPacket.getReasonString();
        final ArrayList<SubackReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final SubackReasonCode code : subackPacket.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
        this.reasonCodes = ImmutableList.copyOf(subAckReasonCodes);
    }

    public boolean isModified() {
        return modified || userProperties.isModified();
    }

    @Override
    public void setReasonString(final @NotNull String reasonString) {
        Preconditions.checkNotNull(reasonString, "Reason string must never be null");
        PluginBuilderUtil.checkReasonString(reasonString, configurationService.securityConfiguration().validateUTF8());
        this.reasonString = reasonString;
        this.modified = true;
    }

    @Override
    public void setReasonCodes(final @NotNull List<SubackReasonCode> reasonCodes) {
        Preconditions.checkNotNull(reasonCodes, "Reason string must never be null");
        if (Objects.equals(this.reasonCodes, reasonCodes)) {
            return;
        }
        if (reasonCodes.size() != this.reasonCodes.size()) {
            throw new IllegalArgumentException("You cannot change the count of reason codes.");
        }
        this.reasonCodes = ImmutableList.copyOf(reasonCodes);
        this.modified = true;
    }

    @Override
    public ImmutableList<SubackReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @Override
    public String getReasonString() {
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
}
