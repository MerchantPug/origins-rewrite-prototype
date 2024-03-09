package com.github.apace100.apoli.power;

import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
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

    public static final Codec<RegistryEntry<Power>> REGISTRY_CODEC = RegistryElementCodec.of(ApoliRegistryKeys.POWER, CODEC);
    public static final Codec<RegistryEntryList<Power>> REGISTRY_LIST_CODEC = RegistryCodecs.entryList(ApoliRegistryKeys.POWER, CODEC);

    private boolean initialized = false;

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

    public void init(DynamicRegistryManager registryManager) {

        if (!initialized) {
            Identifier id = registryManager.get(ApoliRegistryKeys.POWER).getId(this);
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

            this.type.init(this);
            this.initialized = true;
        }

    }
    public void onGained(LivingEntity holder) {
        this.getType().onGained(holder);
    }

    public void onLost(LivingEntity holder) {
        this.getType().onLost(holder);
    }

    public void commonTick(LivingEntity holder) {
        this.getType().commonTick(holder);
    }

    public void serverTick(LivingEntity holder) {
        this.getType().serverTick(holder);
    }

    public void clientTick(LivingEntity holder) {
        this.getType().clientTick(holder);
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
