package com.github.apace100.apoli.power.codec;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.DataAttachedPowerType;
import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PowerDataCodec implements Codec<Map<RegistryEntry<Power>, Object>> {
    public static final PowerDataCodec INSTANCE = new PowerDataCodec();
    private static final Codec<RegistryKey<Power>> REGISTRY_KEY_CODEC = RegistryKey.createCodec(ApoliRegistryKeys.POWER);

    private PowerDataCodec() {

    }

    @Override
    public <T> DataResult<Pair<Map<RegistryEntry<Power>, Object>, T>> decode(DynamicOps<T> ops, T input) {
        final HashMap<RegistryEntry<Power>, Object> map = new HashMap<>();

        if (!(ops instanceof RegistryOps<T> registryOps) || registryOps.getEntryLookup(ApoliRegistryKeys.POWER).isEmpty()) {
            Apoli.LOGGER.warn("PowerDataCodec requires RegistryOps to decode.");
            return DataResult.success(Pair.of(map, input));
        }

        RegistryEntryLookup<Power> powerRegistry = registryOps.getEntryLookup(ApoliRegistryKeys.POWER).get();
        Optional<Stream<Pair<T, T>>> pairStream = ops.getMapValues(input).result();

        if (pairStream.isPresent()) {
            for (Pair<T, T> pair : pairStream.get().toList()) {
                DataResult<Pair<RegistryKey<Power>, T>> key = REGISTRY_KEY_CODEC.decode(ops, pair.getFirst());
                if (key.error().isPresent() || key.result().isEmpty()) {
                    Apoli.LOGGER.warn("Failed to decode power id from {} whilst decoding power data.", pair.getFirst());
                    continue;
                }
                Optional<RegistryEntry.Reference<Power>> power = powerRegistry.getOptional(key.result().get().getFirst());
                if (power.isEmpty()) {
                    Apoli.LOGGER.warn("Failed to get power from id '{}' whilst decoding power data.", key.result().get().getFirst().getValue());
                    continue;
                }
                PowerType powerType = power.get().value().getType();
                if (!(powerType instanceof DataAttachedPowerType<?> dapt)) {
                    Apoli.LOGGER.warn("Power type '{}' cannot have attached data.", ApoliRegistries.POWER_TYPE.getId(powerType.getSerializer()));
                    continue;
                }
                Codec<Object> objectCodec = (Codec<Object>) dapt.getPowerDataCodec();
                DataResult<Pair<Object, T>> value = objectCodec.decode(ops, pair.getSecond());
                if (value.error().isPresent() || value.result().isEmpty()) {
                    Apoli.LOGGER.warn("Failed to decode power data from {}.", pair.getSecond());
                    continue;
                }
                map.put(power.get(), value.result().get().getFirst());
            }
        }

        return DataResult.success(Pair.of(map, input));
    }

    @Override
    public <T> DataResult<T> encode(Map<RegistryEntry<Power>, Object> input, DynamicOps<T> ops, T prefix) {
        Map<T, T> map = new HashMap<>();

        for (final Map.Entry<RegistryEntry<Power>, Object> entry : input.entrySet()) {
            if (entry.getKey().getKey().isEmpty()) {
                Apoli.LOGGER.warn("Failed to get power id from a power whilst encoding power data.");
                continue;
            }
            DataResult<T> encodedKey = REGISTRY_KEY_CODEC.encodeStart(ops, entry.getKey().getKey().get());
            if (encodedKey.error().isPresent()) {
                Apoli.LOGGER.warn("Failed to encode power id '{}' whilst encoding power data. {}", entry.getKey().getKey().get().getValue(), encodedKey.error().get().message());
                continue;
            }
            PowerType powerType = entry.getKey().value().getType();
            if (!(powerType instanceof DataAttachedPowerType<?> dapt)) {
                Apoli.LOGGER.warn("Power type '{}' cannot have attached data.", ApoliRegistries.POWER_TYPE.getId(powerType.getSerializer()));
                continue;
            }
            Codec<Object> objectCodec = (Codec<Object>) dapt.getPowerDataCodec();
            DataResult<T> encodedValue = objectCodec.encodeStart(ops, entry.getValue());
            if (encodedValue.error().isPresent()) {
                Apoli.LOGGER.warn("Failed to encode power data for power '{}'. {}", entry.getKey().getKey().get().getValue(), encodedKey.error().get().message());
                continue;
            }
            map.put(encodedKey.result().get(), encodedValue.result().get());
        }
        return DataResult.success(ops.createMap(map));
    }
}
