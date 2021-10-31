package io.webby.examples.model;

import java.util.Arrays;
import java.util.Objects;

public record StringModel(String id, byte[] rawBytes) {
    @Override
    public boolean equals(Object o) {
        return o instanceof StringModel that && Objects.equals(id, that.id) && Arrays.equals(rawBytes, that.rawBytes);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(id) + Arrays.hashCode(rawBytes);
    }
}
