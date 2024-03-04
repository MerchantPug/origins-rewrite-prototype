package com.github.apace100.apoli.power;

import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public class Power {

    public static final Codec<Power> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PowerType.DISPATCH_CODEC.forGetter(Power::getType),
        Codecs.createStrictOptionalFieldCodec(TextCodecs.CODEC, "name", Text.empty()).forGetter(Power::getName),
        Codecs.createStrictOptionalFieldCodec(TextCodecs.CODEC, "description", Text.empty()).forGetter(Power::getDescription),
        Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Power::isHidden)
    ).apply(instance, Power::new));

    private final PowerType type;
    private final boolean hidden;

    private Text name;
    private Text description;

    public Power(PowerType type, Text name, Text description, boolean hidden) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.hidden = hidden;
    }

    public Power init(LivingEntity holder) {

        Identifier id = holder.getWorld().getRegistryManager().get(ApoliRegistryKeys.POWER).getId(this);
        if (id != null) {

            String namespace = id.getNamespace();
            String path = id.getPath();

            this.name = name.getContent() == PlainTextContent.EMPTY
                ? Text.translatable("power." + namespace + "." + path + ".name")
                : name;
            this.description = description.getContent() == PlainTextContent.EMPTY
                ? Text.translatable("power." + namespace + "." + path + ".description")
                : description;

        }

        this.type.init(holder, this);
        return this;

    }

    public PowerType getType() {
        return type;
    }

    public Text getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    public boolean isHidden() {
        return hidden;
    }

}
