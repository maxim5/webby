package io.webby.util.lazy;

import io.webby.testing.MockSupplier;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class LazyTest {
    @Test
    public void simple() {
        MockSupplier<String> mockSupplier = MockSupplier.mock("foo");

        Lazy<String> lazy = new Lazy<>(mockSupplier);
        assertThat(mockSupplier.timesCalled()).isEqualTo(0);

        assertThat(lazy.get()).isEqualTo("foo");
        assertThat(mockSupplier.timesCalled()).isEqualTo(1);

        assertThat(lazy.get()).isEqualTo("foo");
        assertThat(mockSupplier.timesCalled()).isEqualTo(1);
    }
}
