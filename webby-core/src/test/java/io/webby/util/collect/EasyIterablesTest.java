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

    @Test
    public void getOnlyItemOrEmpty_all_nulls() {
        assertEquals(Stream.of((Object) null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_one_value() {
        assertEquals(Stream.of(1, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(null, 1).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(null, null, 1, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_several_values() {
        assertEquals(Stream.of(1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, null, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, 1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null, 1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }
}
