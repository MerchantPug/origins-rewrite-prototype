package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.calio.api.data.Serializable;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

public abstract class PowerType implements Serializable<PowerType> {
    public static final MapCodec<PowerType> DISPATCH_CODEC = Serializable
        .registryCodec(ApoliRegistries.POWER_TYPE, PowerTypes.ALIASES)
        .dispatchMap("type", Serializable::getSerializer, Serializer::getCodec);
    private Power power;

    @ApiStatus.Internal
    public final void init(Power power) {
        this.power = power;
    }

    public Power getPower() {
        return power;
    }

    public void onGained(LivingEntity holder) {

    }

    public void onLost(LivingEntity holder) {

    }

    public void commonTick(LivingEntity holder) {

    }

    public void serverTick(LivingEntity holder) {

    }

    public void clientTick(LivingEntity holder) {

    }

    public boolean shouldTick() {
        return false;
    }

    public boolean shouldTickWhenInactive() {
        return false;
    }

}
