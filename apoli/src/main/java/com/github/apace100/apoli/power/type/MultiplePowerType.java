package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.power.SubPower;
import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.calio.api.data.Serializable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

public class MultiplePowerType extends PowerType {

    private static final Set<Identifier> DISALLOWED = Set.of(Apoli.identifier("multiple"));
    public static final MapCodec<PowerType> MULTIPLE_DISALLOWING_DISPATCH_CODEC = Serializable
            .valueDisallowingRegistryCodec(ApoliRegistries.POWER_TYPE, PowerTypes.ALIASES, DISALLOWED)
            .dispatchMap("type", Serializable::getSerializer, Serializer::getCodec);

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
