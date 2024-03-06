package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.power.SubPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;

public class MultiplePowerType extends PowerType {
    public static final MapCodec<PowerType> MULTIPLE_DISALLOWING_DISPATCH_CODEC = PowerType.DISPATCH_CODEC
            .flatXmap(powerType -> {
                if (powerType instanceof MultiplePowerType) {
                    return DataResult.error(() -> "Multiple power types may not be nested inside other multiple power types.");
                }
                return DataResult.success(powerType);
            }, DataResult::success);

    public static final Codec<MultiplePowerType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SubPower.CODEC.listOf().fieldOf("subpowers").forGetter(MultiplePowerType::getSubPowers)
    ).apply(instance, MultiplePowerType::new));

    public static final Serializer<MultiplePowerType> SERIALIZER = () -> CODEC;

    private final List<SubPower> subPowers;

    public MultiplePowerType(List<SubPower> subPowers) {
        this.subPowers = subPowers;
    }

    @Override
    public Serializer<? extends PowerType> getSerializer() {
        return SERIALIZER;
    }

    public List<SubPower> getSubPowers() {
        return subPowers;
    }

    public static Factory<MultiplePowerType> getFactory() {
        return new Factory<>(Identifier.of("apoli","multiple"), SERIALIZER);
    }

}
