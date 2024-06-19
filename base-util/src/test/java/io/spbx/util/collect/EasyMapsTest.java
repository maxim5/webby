package io.spbx.util.collect;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EasyMapsTest {
    @Test
    public void asMap_simple() {
        assertThat(EasyMaps.asMap()).isEmpty();
        assertThat(EasyMaps.asMap("foo", "bar")).containsExactly("foo", "bar");
        assertThat(EasyMaps.asMap("foo", "bar", "baz", "bar")).containsExactly("foo", "bar", "baz", "bar");
        assertThat(EasyMaps.asMap("foo", null, "baz", null)).containsExactly("foo", null, "baz", null);
        assertThat(EasyMaps.asMap(null, "foo")).containsExactly(null, "foo");
        assertThat(EasyMaps.asMap(null, null)).containsExactly(null, null);
    }

    @Test
    public void asMap_invalid() {
        assertThrows(AssertionError.class, () -> EasyMaps.asMap("foo"));
        assertThrows(AssertionError.class, () -> EasyMaps.asMap("foo", "bar", "baz"));
        assertThrows(AssertionError.class, () -> EasyMaps.asMap("foo", "bar", "foo", "bar"));
    }

    @Test
    public void immutableOf_simple() {
        assertThat(EasyMaps.immutableOf("foo", "bar")).containsExactly("foo", "bar");
        assertThat(EasyMaps.immutableOf("foo", "bar", "baz", "bar")).containsExactly("foo", "bar", "baz", "bar");
    }

    @Test
    public void immutableOf_invalid() {
        assertThrows(AssertionError.class, () -> EasyMaps.immutableOf("foo", "bar", "baz"));
        assertThrows(IllegalArgumentException.class, () -> EasyMaps.immutableOf("foo", "bar", "foo", "bar"));
    }

    @Test
    public void merge_two_simple() {
        assertThat(EasyMaps.merge(ImmutableMap.of(), ImmutableMap.of())).isEmpty();
        assertThat(EasyMaps.merge(ImmutableMap.of("foo", "bar"), ImmutableMap.of())).containsExactly("foo", "bar");
        assertThat(EasyMaps.merge(ImmutableMap.of(), ImmutableMap.of("foo", "bar"))).containsExactly("foo", "bar");

        assertThat(EasyMaps.merge(ImmutableMap.of("foo", "bar"), ImmutableMap.of("bar", "baz")))
            .containsExactly("foo", "bar", "bar", "baz");
        assertThat(EasyMaps.merge(ImmutableMap.of("foo", "bar"), ImmutableMap.of("foo", "baz")))
            .containsExactly("foo", "baz");
        assertThat(EasyMaps.merge(ImmutableMap.of("foo", "bar"), ImmutableMap.of("bar", "foo")))
            .containsExactly("foo", "bar", "bar", "foo");

        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2, 3, 4), ImmutableMap.of(1, 5, 3, 5)))
            .containsExactly(1, 5, 3, 5);
        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2, 3, 4, 5, 6), ImmutableMap.of(1, 5, 3, 5)))
            .containsExactly(1, 5, 3, 5, 5, 6);
    }

    @Test
    public void merge_three_simple() {
        assertThat(EasyMaps.merge(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of())).isEmpty();
        assertThat(EasyMaps.merge(ImmutableMap.of("foo", "bar"), ImmutableMap.of(), ImmutableMap.of()))
            .containsExactly("foo", "bar");
        assertThat(EasyMaps.merge(ImmutableMap.of(), ImmutableMap.of("foo", "bar"), ImmutableMap.of()))
            .containsExactly("foo", "bar");

        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2), ImmutableMap.of(3, 4), ImmutableMap.of(5, 6)))
            .containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2), ImmutableMap.of(2, 3), ImmutableMap.of(3, 4)))
            .containsExactly(1, 2, 2, 3, 3, 4);
        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2), ImmutableMap.of(1, 3), ImmutableMap.of(1, 4)))
            .containsExactly(1, 4);

        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2, 3, 4), ImmutableMap.of(1, 5, 3, 5), ImmutableMap.of(1, 6, 3, 6)))
            .containsExactly(1, 6, 3, 6);
        assertThat(EasyMaps.merge(ImmutableMap.of(1, 2, 3, 4, 5, 6), ImmutableMap.of(1, 5, 3, 5), ImmutableMap.of(5, 8)))
            .containsExactly(1, 5, 3, 5, 5, 8);
    }

    @Test
    public void mergeToImmutable_two_simple() {
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(), ImmutableMap.of())).isEmpty();
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of("foo", "bar"), ImmutableMap.of()))
            .containsExactly("foo", "bar");
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(), ImmutableMap.of("foo", "bar")))
            .containsExactly("foo", "bar");

        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of("foo", "bar"), ImmutableMap.of("bar", "baz")))
            .containsExactly("foo", "bar", "bar", "baz");
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of("foo", "bar"), ImmutableMap.of("bar", "foo")))
            .containsExactly("foo", "bar", "bar", "foo");

        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of("foo", "bar"), ImmutableMap.of("foo", "baz")));
        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2, 3, 4), ImmutableMap.of(1, 5, 3, 5)));
        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2, 3, 4, 5, 6), ImmutableMap.of(1, 5, 3, 5)));
    }

    @Test
    public void mergeToImmutable_three_simple() {
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of())).isEmpty();
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of("foo", "bar"), ImmutableMap.of(), ImmutableMap.of()))
            .containsExactly("foo", "bar");
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(), ImmutableMap.of("foo", "bar"), ImmutableMap.of()))
            .containsExactly("foo", "bar");

        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2), ImmutableMap.of(3, 4), ImmutableMap.of(5, 6)))
            .containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2), ImmutableMap.of(2, 3), ImmutableMap.of(3, 4)))
            .containsExactly(1, 2, 2, 3, 3, 4);

        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2), ImmutableMap.of(1, 3), ImmutableMap.of(1, 4)));
        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2), ImmutableMap.of(1, 5), ImmutableMap.of(1, 6, 3, 6)));
        assertThrows(IllegalArgumentException.class,
            () -> EasyMaps.mergeToImmutable(ImmutableMap.of(1, 2, 3, 4), ImmutableMap.of(7, 5), ImmutableMap.of(3, 8)));
    }
}
