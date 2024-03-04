package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.calio.api.data.Serializable;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.ApiStatus;

public abstract class PowerType implements Serializable<PowerType> {

    public static final MapCodec<PowerType> DISPATCH_CODEC = Serializable
        .registryCodec(ApoliRegistries.POWER_TYPE, PowerTypes.ALIASES)
        .dispatchMap("type", Serializable::getSerializer, Serializer::getCodec);

    private LivingEntity holder;
    private Power power;

    @ApiStatus.Internal
    public final void init(LivingEntity holder, Power power) {
        this.holder = holder;
        this.power = power;
    }

    public LivingEntity getHolder() {
        return holder;
    }

    public Power getPower() {
        return power;
    }

    public void onGained() {

    }

    public void onLost() {

    }

    public void commonTick() {

    }

    public void serverTick() {

    }

    public void clientTick() {

    }

    public NbtElement toNbt() {
        return new NbtCompound();
    }

    public void fromNbt(NbtElement nbt) {

    }

    public boolean shouldTick() {
        return false;
    }

    public boolean shouldTickWhenInactive() {
        return false;
    }

}
