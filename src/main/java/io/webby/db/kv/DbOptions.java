package io.webby.db.kv;

import io.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DbOptions<K, V>(@NotNull String name,
                              @NotNull Class<K> key,
                              @NotNull Class<V> value,
                              @Nullable StorageType storageType,
                              @Nullable Codec<K> keyCodec,
                              @Nullable Codec<V> valueCodec) {
    public static <K, V> @NotNull DbOptions<K, V> of(@NotNull String name,
                                                     @NotNull Class<K> key,
                                                     @NotNull Class<V> value) {
        return new DbOptions<>(name, key, value, null, null, null);
    }

    public @NotNull DbOptions<K, V> withCustomStorageType(@NotNull StorageType type) {
        return new DbOptions<>(name, key, value, type, keyCodec, valueCodec);
    }

    public @NotNull DbOptions<K, V> withCustomKeyCodec(@NotNull Codec<K> codec) {
        return new DbOptions<>(name, key, value, storageType, codec, valueCodec);
    }

    public @NotNull DbOptions<K, V> withCustomValueCodec(@NotNull Codec<V> codec) {
        return new DbOptions<>(name, key, value, storageType, keyCodec, codec);
    }
}
