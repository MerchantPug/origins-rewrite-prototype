package com.github.apace100.apoli.network.s2c;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.attachment.ApoliAttachmentTypes;
import com.github.apace100.apoli.attachment.PowerHolderAttachment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SyncPowerHolderAttachmentS2CPacket(int entityId, PowerHolderAttachment attachment) implements CustomPayload {

    public static final CustomPayload.Id<SyncPowerHolderAttachmentS2CPacket> ID = CustomPayload.id("apoli:sync_power_attachment");

    private static final Codec<SyncPowerHolderAttachmentS2CPacket> INNER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("entity_id").forGetter(SyncPowerHolderAttachmentS2CPacket::entityId),
            PowerHolderAttachment.CODEC.fieldOf("attachment").forGetter(SyncPowerHolderAttachmentS2CPacket::attachment)
    ).apply(instance, SyncPowerHolderAttachmentS2CPacket::new));
    public static final PacketCodec<RegistryByteBuf, SyncPowerHolderAttachmentS2CPacket> CODEC = PacketCodecs.registryCodec(INNER_CODEC).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle() {

        MinecraftClient.getInstance().execute(() -> {
            Entity entity = MinecraftClient.getInstance().world.getEntityById(entityId());

            if (!(entity instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Attempted to sync powers for a non living entity.");
                return;
            }

            entity.setAttached(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT, attachment());
        });

    }
}
