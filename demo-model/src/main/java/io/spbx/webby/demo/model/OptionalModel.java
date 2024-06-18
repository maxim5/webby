package io.spbx.webby.demo.model;

import java.util.Objects;
import java.util.Optional;

public record OptionalModel(int id, Optional<Integer> i, Optional<Long> l, Optional<String> str) {
    @Override
    public boolean equals(Object o) {
        return o instanceof OptionalModel that &&
            Objects.equals(id, that.id) &&
            Objects.equals(i.orElse(0), that.i.orElse(0)) &&
            Objects.equals(l.orElse(0L), that.l.orElse(0L)) &&
            Objects.equals(str.orElse(""), that.str.orElse(""));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, i.orElse(0), l.orElse(0L), str.orElse(""));
    }
}
