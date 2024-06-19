package io.spbx.util.lazy;

import io.spbx.util.testing.MockSupplier;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class LazyBooleanTest {
    @Test
    public void simple() {
        MockSupplier.Bool mockSupplier = MockSupplier.Bool.of(true);

        LazyBoolean lazy = new LazyBoolean(mockSupplier);
        assertThat(mockSupplier.timesCalled()).isEqualTo(0);

        assertThat(lazy.get()).isTrue();
        assertThat(mockSupplier.timesCalled()).isEqualTo(1);

        assertThat(lazy.get()).isTrue();
        assertThat(mockSupplier.timesCalled()).isEqualTo(1);
    }
}
