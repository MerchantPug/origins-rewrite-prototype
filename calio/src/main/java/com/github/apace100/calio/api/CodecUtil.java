package com.github.apace100.calio.api;

import com.github.apace100.calio.api.codec.ForgivingMapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CodecUtil {

    public static <T, L extends List<T>> Codec<List<T>> listCodec(Codec<T> codec, Function<List<T>, L> constructor) {
        return codec.listOf().xmap(constructor, list -> list);
    }

    public static <K, V, L extends Map<K, V>> Codec<Map<K, V>> mapCodec(Codec<K> keyCodec, Codec<V> valueCodec, Keyable keyable, Function<Map<K, V>, L> constructor) {
        return Codec.simpleMap(keyCodec, valueCodec, keyable).xmap(kvMap -> (Map<K, V>)constructor.apply(kvMap), list -> list).codec();
    }

    public static <K, V, L extends Map<K, V>> Codec<Map<K, V>> forgivingMapCodec(Codec<K> keyCodec, Codec<V> valueCodec, Function<Map<K, V>, L> constructor) {
        return new ForgivingMapCodec<>(keyCodec, valueCodec, null, null).xmap(constructor, list -> list);
    }

    public static <K, V, L extends Map<K, V>> Codec<Map<K, V>> forgivingMapCodec(Codec<K> keyCodec, Codec<V> valueCodec, Consumer<String> onKeyError, Consumer<String> onValueError, Function<Map<K, V>, L> constructor) {
        return new ForgivingMapCodec<>(keyCodec, valueCodec, onKeyError, onValueError).xmap(constructor, list -> list);
    }

}
