package io.webby.demo.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public record AtomicModel(int id, AtomicInteger i, AtomicLong l, AtomicBoolean b, AtomicReference<String> s) {
    @Override
    public boolean equals(Object o) {
        return o instanceof AtomicModel that &&
            id == that.id && i.get() == that.i.get() &&
            l.get() == that.l.get() && b.get() == that.b.get() &&
            Objects.equals(s.get(), that.s.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, i.get(), l.get(), b.get(), s.get());
    }
}
