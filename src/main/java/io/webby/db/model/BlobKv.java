package io.webby.db.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public record BlobKv(byte @NotNull [] id, byte @Nullable [] value) {
    public static final String DB_NAME = "blob_kv";

    @Override
    public boolean equals(Object o) {
        return o instanceof BlobKv blobKv && Arrays.equals(id, blobKv.id) && Arrays.equals(value, blobKv.value);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(id) + Arrays.hashCode(value);
    }
}
