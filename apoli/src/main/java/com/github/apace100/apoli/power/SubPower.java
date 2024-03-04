package com.github.apace100.apoli.power;

import com.github.apace100.apoli.power.type.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class SubPower extends Power {

    public static final Codec<SubPower> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PowerType.DISPATCH_CODEC.forGetter(Power::getType),
        Identifier.CODEC.fieldOf("id").forGetter(SubPower::getId),
        Codecs.createStrictOptionalFieldCodec(TextCodecs.CODEC, "name", Text.empty()).forGetter(Power::getName),
        Codecs.createStrictOptionalFieldCodec(TextCodecs.CODEC, "description", Text.empty()).forGetter(Power::getDescription)
    ).apply(instance, SubPower::new));

    private final Identifier id;

    public SubPower(PowerType type, Identifier id, Text name, Text description) {
        super(type, name, description, true);
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

}
