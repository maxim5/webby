package io.webby.examples.model;

import java.util.Arrays;
import java.util.Objects;

public record PojoWithAdapterModel(int id, Pojo pojo) {

    record Pojo(int id, char[] buf) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Pojo that && Objects.equals(id, that.id) && Arrays.equals(buf, that.buf);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id, Arrays.hashCode(buf));
        }
    }
}
