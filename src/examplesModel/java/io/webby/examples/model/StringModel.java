package io.webby.examples.model;

import java.util.Arrays;
import java.util.Objects;

public record StringModel(String id, CharSequence sequence, char[] chars, byte[] rawBytes) {
    @Override
    public boolean equals(Object o) {
        return o instanceof StringModel that &&
               Objects.equals(id, that.id) && Objects.equals(sequence, that.sequence) &&
               Arrays.equals(chars, that.chars) && Arrays.equals(rawBytes, that.rawBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(chars), Arrays.hashCode(rawBytes));
    }
}
