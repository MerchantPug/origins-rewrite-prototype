package com.github.apace100.apoli.power.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

// TODO: Potentially allow for more types of variables to be stored in this power when value providers happen. - Pug
public class ResourcePowerType extends PowerType implements DataAttachedPowerType<ResourcePowerType.ResourcePowerData> {
    public static final Codec<ResourcePowerType> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("min", 0).forGetter(ResourcePowerType::getMin),
                Codec.INT.optionalFieldOf("max", 1).forGetter(ResourcePowerType::getMax)
        ).apply(instance, ResourcePowerType::new));

    public static final Serializer<ResourcePowerType> SERIALIZER = () -> CODEC;

    private final int min;
    private final int max;

    public ResourcePowerType(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void onGained(LivingEntity holder) {
        getOrCreatePowerData(holder);
    }

    @Override
    public Serializer<? extends PowerType> getSerializer() {
        return SERIALIZER;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue(LivingEntity entity) {
        return getOrCreatePowerData(entity).getValue();
    }

    public int setValue(LivingEntity entity, int value) {
        int newValue = MathHelper.clamp(value, min, max);
        getOrCreatePowerData(entity).setValue(newValue);
        return newValue;
    }

    @Override
    public Codec<ResourcePowerData> getPowerDataCodec() {
        return ResourcePowerData.CODEC;
    }

    @Override
    public ResourcePowerData getPowerDataDefaultValue() {
        return new ResourcePowerData(this.min);
    }

    public static Factory<ResourcePowerType> getFactory() {
        return new Factory<>(Identifier.of("apoli", "resource"), SERIALIZER);
    }

    protected static class ResourcePowerData {
        public static final Codec<ResourcePowerData> CODEC = Codec.INT.xmap(ResourcePowerData::new, ResourcePowerData::getValue);

        private int value;

        public ResourcePowerData(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
