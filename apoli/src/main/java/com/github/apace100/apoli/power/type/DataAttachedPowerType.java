package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.attachment.ApoliEntityApis;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;

public interface DataAttachedPowerType<D> {

    Codec<D> getPowerDataCodec();

    D getPowerDataDefaultValue();

    Power getPower();

    default D getPowerData(LivingEntity holder) {
        return ApoliEntityApis.POWER_HOLDER.find(holder, null).getPowerData(holder.getWorld().getRegistryManager().get(ApoliRegistryKeys.POWER).getEntry(getPower()));
    }

    default D getOrCreatePowerData(LivingEntity holder) {
        return ApoliEntityApis.POWER_HOLDER.find(holder, null).getOrCreatePowerData(holder.getWorld().getRegistryManager().get(ApoliRegistryKeys.POWER).getEntry(getPower()));
    }

}
