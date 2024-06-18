package io.spbx.util.lazy;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class AtomicLazyRecycleTest {
    @Test
    public void not_initialized() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        assertThat(lazy.isInitialized()).isFalse();
        assertThrows(NullPointerException.class, () -> lazy.getOrDie());
    }

    @Test
    public void initialize_or_die_simple() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.initializeOrDie("foo");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("foo");
    }

    @Test
    public void initialize_or_die_twice() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.initializeOrDie("foo");
        assertThrows(AssertionError.class, () -> lazy.initializeOrDie("foo"));
    }

    @Test
    public void recycle_uninitialized() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.recycle();
        assertThat(lazy.isInitialized()).isFalse();
        assertThrows(NullPointerException.class, () -> lazy.getOrDie());
    }

    @Test
    public void recycle_simple() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.initializeOrDie("foo");
        lazy.recycle();
        assertThat(lazy.isInitialized()).isFalse();
        assertThrows(NullPointerException.class, () -> lazy.getOrDie());
    }

    @Test
    public void recycle_then_initialize() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.initializeOrDie("foo");
        lazy.recycle();
        lazy.initializeOrDie("bar");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("bar");
    }

    @Test
    public void recycle_consume() {
        AtomicLazyRecycle<String> lazy = new AtomicLazyRecycle<>(null);
        lazy.recycle(value -> fail("Must not be called"));

        lazy.initializeOrDie("foo");
        lazy.recycle(value -> assertThat(value).isEqualTo("foo"));
    }
}
