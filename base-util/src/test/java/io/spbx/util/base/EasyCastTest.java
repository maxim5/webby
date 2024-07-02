package io.spbx.util.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EasyCastTest {
    @Test
    public void castAny_simple() {
        Integer integer = EasyCast.castAny((String) null);
        assertThat(integer).isNull();

        Object[] objects = EasyCast.castAny(new Integer[] { 1, 2 });
        assertThat(objects).asList().containsExactly(1, 2);

        assertThrows(ClassCastException.class, () -> {
            Integer[] ignore = EasyCast.castAny(new Object[] { 1, 2 });
        });
    }
}
