package io.webby.util.lazy;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AtomicLazyInitTest {
    @Test
    public void not_initialized() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        assertThat(lazy.isInitialized()).isFalse();
        assertThrows(NullPointerException.class, () -> lazy.getOrDie());
    }

    @Test
    public void initialize_or_die_simple() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        lazy.initializeOrDie("foo");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("foo");
    }

    @Test
    public void initialize_or_die_twice() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        lazy.initializeOrDie("foo");
        assertThrows(AssertionError.class, () -> lazy.initializeOrDie("foo"));
    }

    @Test
    public void initialize_compare_from_scratch() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        lazy.initializeOrCompare("bar");
        lazy.initializeOrCompare("bar");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("bar");
    }

    @Test
    public void initialize_compare_from_initialized() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        lazy.initializeOrDie("baz");
        lazy.initializeOrCompare("baz");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("baz");
    }

    @Test
    public void initialize_compare_mismatch() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        lazy.initializeOrDie("foo");
        assertThrows(AssertionError.class, () -> lazy.initializeOrDie("bar"));
    }

    @Test
    public void initialize_if_not_yet() {
        AtomicLazyInit<String> lazy = new AtomicLazyInit<>(null);
        assertThat(lazy.initializeIfNotYet(() -> "foo")).isEqualTo("foo");
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(lazy.getOrDie()).isEqualTo("foo");
        assertThat(lazy.initializeIfNotYet(() -> "bar")).isEqualTo("foo");
    }
}
