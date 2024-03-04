package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.power.SubPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;

public class MultiplePowerType extends PowerType {

    public static final Codec<MultiplePowerType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SubPower.CODEC.listOf().fieldOf("subpowers").forGetter(MultiplePowerType::getSubPowers)
    ).apply(instance, MultiplePowerType::new));

    private final List<SubPower> subPowers;

    public MultiplePowerType(List<SubPower> subPowers) {
        this.subPowers = subPowers;
    }

    @Override
    public Serializer<? extends PowerType> getSerializer() {
        return () -> CODEC;
    }

    public List<SubPower> getSubPowers() {
        return subPowers;
    }

    public static Factory<MultiplePowerType> getFactory() {
        return new Factory<>(Identifier.of("apoli","multiple"), () -> CODEC);
    }

}
