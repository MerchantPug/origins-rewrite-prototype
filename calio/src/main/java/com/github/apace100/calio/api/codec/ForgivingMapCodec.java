package com.github.apace100.calio.api.codec;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>
 * A map codec that will not resolve a value if either the key or value errors.
 * </p>
 * @param <K>   The key of the returned map.
 * @param <V>   The value of the returned map.
 */
public class ForgivingMapCodec<K, V> implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
    private final Codec<K> keyCodec;
    private final Codec<V> elementCodec;
    private final Consumer<String> onKeyError;
    private final Consumer<String> onValueError;

    public ForgivingMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec,
                             @Nullable Consumer<String> onKeyError,
                             @Nullable Consumer<String> onValueError) {
        this.keyCodec = keyCodec;
        this.elementCodec = elementCodec;
        this.onKeyError = onKeyError;
        this.onValueError = onValueError;
    }

    @Override
    public Codec<K> keyCodec() {
        return keyCodec;
    }

    @Override
    public Codec<V> elementCodec() {
        return elementCodec;
    }

    public Consumer<String> onKeyError() {
        return onKeyError;
    }

    public Consumer<String> onValueError() {
        return onValueError;
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decodeInternal(ops, map)).map(r -> Pair.of(r, input));
    }

    private <T> DataResult<Map<K, V>> decodeInternal(final DynamicOps<T> ops, final MapLike<T> input) {
        final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();

        for (Pair<T, T> pair : input.entries().toList()) {

            final DataResult<K> k = keyCodec().parse(ops, pair.getFirst());
            final DataResult<V> v = elementCodec().parse(ops, pair.getSecond());

            if (k.error().isPresent()) {
                if (onKeyError() != null) {
                    onKeyError().accept(k.error().get().message());
                }
                continue;
            }

            if (v.error().isPresent()) {
                if (onValueError() != null) {
                    onValueError().accept(v.error().get().message());
                }
                continue;
            }

            read.put(k.result().get(), v.result().get());
        }

        final Map<K, V> elements = read.build();

        return DataResult.success(elements);
    }

    @Override
    public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ForgivingMapCodec<?, ?> that = (ForgivingMapCodec<?, ?>) o;
        return Objects.equals(keyCodec, that.keyCodec) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodec, elementCodec);
    }

    @Override
    public String toString() {
        return "ForgivingMapCodec[" + keyCodec + " -> " + elementCodec + ']';
    }
}
