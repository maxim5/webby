package io.webby.examples.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public record AtomicModel(int id, AtomicInteger i) {
    @Override
    public boolean equals(Object o) {
        return o instanceof AtomicModel that && id == that.id && i.get() == that.i.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, i.get());
    }
}
