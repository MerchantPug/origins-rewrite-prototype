package com.github.apace100.apoli.power.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;

public class SimplePowerType extends PowerType {

    public static final Codec<SimplePowerType> CODEC = MapCodec
        .of(Encoder.empty(), Decoder.unit(SimplePowerType::new))
        .codec();

    @Override
    public Serializer<? extends PowerType> getSerializer() {
        return () -> CODEC;
    }

    public static Factory<SimplePowerType> getFactory() {
        PowerTypes.ALIASES.addPathAlias("shrimple", "simple"); //  FIXME: For testing stuff. Remove when done -eggohito
        return new Factory<>(Identifier.of("apoli", "simple"), () -> CODEC);
    }

}
