package io.spbx.webby.db.kv;

import io.spbx.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DbOptions<K, V>(@NotNull String name,
                              @NotNull Class<K> key,
                              @NotNull Class<V> value,
                              @Nullable DbType type,
                              @Nullable Codec<K> keyCodec,
                              @Nullable Codec<V> valueCodec) {
    public static <K, V> @NotNull DbOptions<K, V> of(@NotNull String name,
                                                     @NotNull Class<K> key,
                                                     @NotNull Class<V> value) {
        return new DbOptions<>(name, key, value, null, null, null);
    }

    public @NotNull DbOptions<K, V> withCustomType(@NotNull DbType type) {
        return new DbOptions<>(name, key, value, type, keyCodec, valueCodec);
    }

    public @NotNull DbOptions<K, V> withCustomKeyCodec(@NotNull Codec<K> codec) {
        return new DbOptions<>(name, key, value, type, codec, valueCodec);
    }

    public @NotNull DbOptions<K, V> withCustomValueCodec(@NotNull Codec<V> codec) {
        return new DbOptions<>(name, key, value, type, keyCodec, codec);
    }
}
