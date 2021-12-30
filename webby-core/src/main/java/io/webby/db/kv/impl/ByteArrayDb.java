package io.webby.db.kv.impl;

import io.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.AbstractMap;

public abstract class ByteArrayDb<K, V> {
    protected final Codec<K> keyCodec;
    protected final Codec<V> valueCodec;

    public ByteArrayDb(@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    protected byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(key);
    }

    protected @NotNull ByteBuffer directBufferFromKey(@NotNull K key) {
        byte[] bytes = fromKey(key);
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    protected @Nullable K asKey(byte @Nullable [] bytes) {
        return bytes == null ? null : asKeyNotNull(bytes);
    }

    protected @NotNull K asKeyNotNull(byte @NotNull [] bytes) {
        return keyCodec.readFromBytes(bytes);
    }

    protected @Nullable K asKey(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : keyCodec.readFromByteBuffer(buffer);
    }

    protected byte @NotNull [] fromValue(@NotNull V value) {
        return valueCodec.writeToBytes(value);
    }

    protected @NotNull ByteBuffer directBufferFromValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    protected @Nullable V asValue(byte @Nullable [] bytes) {
        return bytes == null ? null : asValueNotNull(bytes);
    }

    protected @NotNull V asValueNotNull(byte @NotNull [] bytes) {
        return valueCodec.readFromBytes(bytes);
    }

    protected @Nullable V asValue(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : valueCodec.readFromByteBuffer(buffer);
    }

    protected @NotNull AbstractMap.SimpleEntry<K, V> asMapEntry(@Nullable K key, @Nullable V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected @NotNull AbstractMap.SimpleEntry<K, V> asMapEntry(byte @Nullable [] key, byte @Nullable [] value) {
        return new AbstractMap.SimpleEntry<>(asKey(key), asValue(value));
    }

    protected @NotNull AbstractMap.SimpleEntry<K, V> asMapEntry(@Nullable ByteBuffer key, @Nullable ByteBuffer value) {
        return new AbstractMap.SimpleEntry<>(asKey(key), asValue(value));
    }
}
