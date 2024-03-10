package com.github.apace100.apoli.attachment;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.codec.PowerDataCodec;
import com.github.apace100.apoli.power.type.DataAttachedPowerType;
import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.calio.api.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PowerHolderAttachment {

    public static final Identifier ID = Apoli.identifier("power_holder");

    private final Map<RegistryEntry<Power>, List<Identifier>> powers;
    private final Map<RegistryEntry<Power>, Object> powerData;

    public static final Codec<PowerHolderAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.forgivingMapCodec(Power.REGISTRY_CODEC, CodecUtil.listCodec(Identifier.CODEC, ArrayList::new), s -> Apoli.LOGGER.warn("Could not resolve power id within powers attachment. {}", s), s -> Apoli.LOGGER.warn("Could not resolve power sources within powers attachment. {}", s), LinkedHashMap::new).optionalFieldOf("powers", new LinkedHashMap<>()).forGetter(PowerHolderAttachment::getPowersWithSources),
                    PowerDataCodec.INSTANCE.optionalFieldOf("data", new HashMap<>()).forGetter(PowerHolderAttachment::getPowerDataMap)
            ).apply(instance, PowerHolderAttachment::new));

    public PowerHolderAttachment(Map<RegistryEntry<Power>, List<Identifier>> powers, Map<RegistryEntry<Power>, Object> powerData) {
        this.powers = powers;
        this.powerData = powerData;
    }

    protected List<RegistryEntry<Power>> getPowers() {
        return List.copyOf(powers.keySet());
    }

    protected void addPower(RegistryEntry<Power> power, Identifier source) {
        powers.computeIfAbsent(power, entry -> new ArrayList<>()).add(source);
    }

    protected void removePower(RegistryEntry<Power> power) {
        powers.remove(power);
    }

    protected List<RegistryEntry<Power>> removeAllPowersFromSource(Identifier source) {
        List<RegistryEntry<Power>> removedPowers = new ArrayList<>();

        for (Map.Entry<RegistryEntry<Power>, List<Identifier>> power : powers.entrySet().stream().filter(entry -> entry.getValue().contains(source)).toList()) {
            removedPowers.add(power.getKey());
            if (revokePower(power.getKey(), source)) {
                powers.remove(power.getKey());
            }
        }

        return removedPowers;
    }

    protected boolean revokePower(RegistryEntry<Power> power, Identifier source) {
        if (!powers.containsKey(power) || !powers.get(power).contains(source)) {
            return false;
        }
        powers.get(power).remove(source);
        if (powers.get(power).isEmpty()) {
            powers.remove(power);
            powerData.remove(power);
            return true;
        }
        return false;
    }

    protected List<RegistryEntry<Power>> clearPowers() {
        List<RegistryEntry<Power>> cleared = getPowers();
        powers.clear();
        powerData.clear();
        return cleared;
    }

    protected boolean hasPower(RegistryEntry<Power> power) {
        return powers.containsKey(power);
    }

    protected boolean hasPower(RegistryEntry<Power> power, Identifier source) {
        return powers.containsKey(power) && powers.get(power).contains(source);
    }

    protected List<Identifier> getSources(RegistryEntry<Power> power) {
        return powers.getOrDefault(power, List.of());
    }

    private Map<RegistryEntry<Power>, List<Identifier>> getPowersWithSources() {
        return Map.copyOf(powers);
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
