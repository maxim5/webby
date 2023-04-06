package io.webby.util.collect;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EasyIterablesTest {
    @Test
    public void getOnlyItemOrEmpty_simple() {
        assertEquals(Stream.of().collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(1, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, 2, 3).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, 2, 3, 4).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }
}
