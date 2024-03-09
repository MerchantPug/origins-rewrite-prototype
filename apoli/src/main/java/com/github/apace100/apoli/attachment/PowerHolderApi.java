package com.github.apace100.apoli.attachment;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.network.s2c.SyncPowerHolderAttachmentS2CPacket;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.calio.api.data.Serializable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class PowerHolderApi {

    public static final Identifier ID = Apoli.identifier("power_holder");

    private final LivingEntity holder;

    public PowerHolderApi(LivingEntity holder) {
        this.holder = holder;
    }

    public <T extends PowerType> T getPowerType(RegistryEntry<Power> registryEntry) {
        return (T) registryEntry.value().getType();
    }

    public boolean hasPower(RegistryEntry<Power> power) {
        return holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).hasPower(power);
    }

    public boolean hasPowerWithSource(RegistryEntry<Power> power, Identifier source) {
        return holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).hasPower(power, source);
    }

    public void addPower(RegistryEntry<Power> power, Identifier source) {
        holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).addPower(power, source);
        power.value().onGained(holder);
    }

    public void removePower(RegistryEntry<Power> power, Identifier source) {
        holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).removePower(power, source);
        power.value().onLost(holder);
    }

    public List<RegistryEntry<Power>> getPowers(Serializable.Serializer<? extends PowerType> powerType, boolean includeInactive) {
        List<RegistryEntry<Power>> allPowers = holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).getPowers();
        return allPowers.stream().filter(entry -> entry.value().getType().getSerializer() == powerType).toList();
    }

    public List<RegistryEntry<Power>> powerList() {
        return powerList(null);
    }

    public List<RegistryEntry<Power>> powerList(@Nullable Identifier source) {
        PowerHolderAttachment attachment = holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT);
        List<RegistryEntry<Power>> allPowers = attachment.getPowers();
        return allPowers.stream().filter(entry -> source == null || attachment.hasPower(entry, source)).toList();
    }

    public List<Identifier> getSources(RegistryEntry<Power> power) {
        return holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).getSources(power);
    }

    public <D> D getPowerData(RegistryEntry<Power> power) {
        return holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).getPowerData(power);
    }

    public <D> D getOrCreatePowerData(RegistryEntry<Power> power) {
        return holder.getAttachedOrCreate(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT).getOrCreatePowerData(power);
    }

    public void sync() {

        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(holder))
            if (otherPlayer.networkHandler != null)
                ServerPlayNetworking.send(otherPlayer, new SyncPowerHolderAttachmentS2CPacket(holder.getId(), holder.getAttached(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT)));

        if (holder instanceof ServerPlayerEntity player && player.networkHandler != null)
            ServerPlayNetworking.send(player, new SyncPowerHolderAttachmentS2CPacket(holder.getId(), holder.getAttached(ApoliAttachmentTypes.POWER_HOLDER_ATTACHMENT)));

    }
}
