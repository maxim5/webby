package io.webby.examples.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public record AtomicModel(int id, AtomicInteger i, AtomicLong l, AtomicBoolean b) {
    @Override
    public boolean equals(Object o) {
        return o instanceof AtomicModel that && id == that.id &&
               i.get() == that.i.get() && l.get() == that.l.get() && b.get() == that.b.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, i.get(), l.get(), b.get());
    }
}
