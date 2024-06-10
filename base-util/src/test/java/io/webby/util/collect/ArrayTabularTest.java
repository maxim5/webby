package io.webby.util.collect;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayTabularTest {
    @Test
    public void of_not_empty_array() {
        Tabular<String> tab = ArrayTabular.of(
            array("first"),
            array("second")
        );

        assertThat(tab.rows()).isEqualTo(2);
        assertThat(tab.columns()).isEqualTo(1);
        assertThat(tab.isEmpty()).isFalse();
        assertThat(tab.cell(0, 0)).isEqualTo("first");
        assertThat(tab.cell(1, 0)).isEqualTo("second");
        assertThat(tab.rowAt(0)).containsExactly("first");
        assertThat(tab.rowAt(1)).containsExactly("second");
        assertThat(tab.columnAt(0)).containsExactly("first", "second");
    }

    @Test
    public void index_out_of_bounds() {
        Tabular<String> tab = ArrayTabular.of(
            array("first"),
            array("second")
        );

        assertThrows(AssertionError.class, () -> tab.cell(0, 1));
        assertThrows(AssertionError.class, () -> tab.cell(1, 1));
        assertThrows(AssertionError.class, () -> tab.rowAt(2));
        assertThrows(AssertionError.class, () -> tab.columnAt(1));
    }
}
