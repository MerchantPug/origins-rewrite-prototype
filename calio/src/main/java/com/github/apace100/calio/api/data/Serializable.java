package com.github.apace100.calio.api.data;

import com.github.apace100.calio.api.IdentifierAlias;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;

/**
 *  <p>An interface used for dispatching a {@link Codec codec} to (de)serialize the instance of the implementing class. This is mostly used
 *  for serializable objects in registries (e.g: objects from a {@link net.fabricmc.fabric.api.event.registry.DynamicRegistries dynamic registry}.)
 *  </p>
 *
 *  <p>TODO: Maybe document this interface better? -eggohito</p>
 */
public interface Serializable<T> {

    Serializer<? extends T> getSerializer();

    interface Serializer<T> {
        Codec<? extends T> getCodec();
    }

    static <T extends Serializable.Serializer<?>> Codec<T> registryCodec(@NotNull Registry<T> registry, @NotNull IdentifierAlias aliases) {

        RegistryKey<? extends Registry<T>> registryKey = registry.getKey();

        return Codecs.withLifecycle(
            Identifier.CODEC.comapFlatMap(
                id -> registry.getEntry(aliases.resolveAliasUntil(id, registry::containsId))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Type \"" + id + "\" is not registered in registry \"" + registryKey.getValue() + "\"")),
                entry ->
                    entry.registryKey().getValue()
            ),
            entry -> registry.getEntryInfo(entry.registryKey())
                .map(RegistryEntryInfo::lifecycle)
                .orElse(Lifecycle.experimental())
        ).flatComapMap(
            RegistryEntry.Reference::value,
            value -> registry.getEntry(value) instanceof RegistryEntry.Reference<T> reference
                    ? DataResult.success(reference)
                    : DataResult.error(() -> "Element \"" + value + "\" is not registered in registry \"" + registryKey.getValue() + "\"")
        );

    }

    record Factory<T>(Identifier id, Serializer<T> serializer) {

    }

    interface Supplier<T> {
        Factory<T> getFactory();
    }

}
