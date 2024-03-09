package com.github.apace100.apoli.attachment;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.codec.PowerDataCodec;
import com.github.apace100.apoli.power.type.DataAttachedPowerType;
import com.github.apace100.apoli.power.type.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PowerHolderAttachment {

    public static final Identifier ID = Apoli.identifier("power_holder");

    private final List<RegistryEntry<Power>> powers;
    private final Map<RegistryEntry<Power>, List<Identifier>> powerSources;
    private final Map<RegistryEntry<Power>, Object> powerData;

    public static final Codec<PowerHolderAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Power.REGISTRY_CODEC.listOf().optionalFieldOf("powers", new ArrayList<>()).xmap(entries -> (List<RegistryEntry<Power>>)new ArrayList<>(entries), entries -> entries).forGetter(PowerHolderAttachment::getPowers),
                    Codec.simpleMap(Power.REGISTRY_CODEC, Identifier.CODEC.listOf().xmap(sources -> (List<Identifier>)new ArrayList<>(sources), sources -> sources), Keyable.forStrings(() -> Stream.of("power", "sources"))).fieldOf("sources").xmap(map -> (Map<RegistryEntry<Power>, List<Identifier>>)new HashMap<>(map), map -> map).forGetter(PowerHolderAttachment::getPowerSources),
                    PowerDataCodec.INSTANCE.optionalFieldOf("power_data", new HashMap<>()).xmap(map -> (Map<RegistryEntry<Power>, Object>)new HashMap<>(map), map -> map).forGetter(PowerHolderAttachment::getPowerDataMap)
            ).apply(instance, PowerHolderAttachment::new));

    public PowerHolderAttachment(List<RegistryEntry<Power>> powers, Map<RegistryEntry<Power>, List<Identifier>> powerSources, Map<RegistryEntry<Power>, Object> powerData) {
        this.powers = powers;
        this.powerSources = powerSources;
        this.powerData = powerData;
    }

    protected List<RegistryEntry<Power>> getPowers() {
        return List.copyOf(powers);
    }

    protected void addPower(RegistryEntry<Power> power, Identifier source) {
        powers.add(power);
        powerSources.computeIfAbsent(power, entry -> new ArrayList<>()).add(source);
    }

    protected void removePower(RegistryEntry<Power> power, Identifier source) {
        powerSources.get(power).remove(source);
        if (powerSources.get(power).isEmpty()) {
            powerSources.remove(power);
            powers.remove(power);
            powerData.remove(power);
        }
    }

    protected void clearPowers() {
        powers.clear();
        powerSources.clear();
        powerData.clear();
    }

    protected boolean hasPower(RegistryEntry<Power> power) {
        return powers.contains(power);
    }

    protected boolean hasPower(RegistryEntry<Power> power, Identifier source) {
        return powers.contains(power) && powerSources.containsKey(power) && powerSources.get(power).contains(source);
    }

    protected List<Identifier> getSources(RegistryEntry<Power> power) {
        return powerSources.getOrDefault(power, List.of());
    }

    private Map<RegistryEntry<Power>, List<Identifier>> getPowerSources() {
        return Map.copyOf(powerSources);
    }

    private Map<RegistryEntry<Power>, Object> getPowerDataMap() {
        return Map.copyOf(powerData);
    }

    protected <D> D getPowerData(RegistryEntry<Power> power) {
        PowerType powerType = power.value().getType();
        if (!(powerType instanceof DataAttachedPowerType<?>)) {
            throw new UnsupportedOperationException("Power " + power.getKey().get().getValue() + " does not support power data.");
        }
        return (D) this.powerData.getOrDefault(power, null);
    }

    protected <D> D getOrCreatePowerData(RegistryEntry<Power> power) {
        PowerType powerType = power.value().getType();
        if (!(powerType instanceof DataAttachedPowerType<?> dataPowerType)) {
            throw new UnsupportedOperationException("Power " + power.getKey().get().getValue() + " does not support power data.");
        }
        return (D) this.powerData.computeIfAbsent(power, entry -> dataPowerType.getPowerDataDefaultValue());
    }

}
