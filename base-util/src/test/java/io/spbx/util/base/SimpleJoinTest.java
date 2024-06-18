package io.spbx.util.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class SimpleJoinTest {
    @Test
    public void strings_all_empty() {
        assertThat(SimpleJoin.of().join()).isEqualTo("");
        assertThat(SimpleJoin.of("").join()).isEqualTo("");
        assertThat(SimpleJoin.of("", "").join()).isEqualTo("");
        assertThat(SimpleJoin.of("", "", "").join()).isEqualTo("");
    }

    @Test
    public void strings_all_empty_join_with_separator() {
        assertThat(SimpleJoin.of().join("_")).isEqualTo("");
        assertThat(SimpleJoin.of("").join("_")).isEqualTo("");
        assertThat(SimpleJoin.of("", "").join("_")).isEqualTo("_");
        assertThat(SimpleJoin.of("", "", "").join("_")).isEqualTo("__");
    }

    @Test
    public void strings_some_empty() {
        assertThat(SimpleJoin.of("a").join()).isEqualTo("a");
        assertThat(SimpleJoin.of("a", "").join()).isEqualTo("a");
        assertThat(SimpleJoin.of("", "a", "").join()).isEqualTo("a");
    }

    @Test
    public void strings_some_empty_join_with_separator() {
        assertThat(SimpleJoin.of("a").join("_")).isEqualTo("a");
        assertThat(SimpleJoin.of("a", "").join("_")).isEqualTo("a_");
        assertThat(SimpleJoin.of("", "a", "").join("_")).isEqualTo("_a_");
    }

    @Test
    public void strings_some_empty_only_non_empty_join_with_separator() {
        assertThat(SimpleJoin.of("a").onlyNonEmpty().join("_")).isEqualTo("a");
        assertThat(SimpleJoin.of("a", "").onlyNonEmpty().join("_")).isEqualTo("a");
        assertThat(SimpleJoin.of("", "a", "").onlyNonEmpty().join("_")).isEqualTo("a");
    }

    @Test
    public void objects_some_empty() {
        assertThat(SimpleJoin.of(1).join()).isEqualTo("1");
        assertThat(SimpleJoin.of(1, 2).join()).isEqualTo("12");
        assertThat(SimpleJoin.of(1, "", 2, 3).join()).isEqualTo("123");
        assertThat(SimpleJoin.of("", "", 1, "").join()).isEqualTo("1");
    }

    @Test
    public void objects_some_empty_join_with_separator() {
        assertThat(SimpleJoin.of(1).join("_")).isEqualTo("1");
        assertThat(SimpleJoin.of(1, 2).join("_")).isEqualTo("1_2");
        assertThat(SimpleJoin.of(1, "", 2, 3).join("_")).isEqualTo("1__2_3");
        assertThat(SimpleJoin.of("", "", 1, "").join("_")).isEqualTo("__1_");
    }

    @Test
    public void objects_some_empty_only_non_empty_join_with_separator() {
        assertThat(SimpleJoin.of(1).onlyNonEmpty().join("_")).isEqualTo("1");
        assertThat(SimpleJoin.of(1, 2).onlyNonEmpty().join("_")).isEqualTo("1_2");
        assertThat(SimpleJoin.of(1, "", 2, 3).onlyNonEmpty().join("_")).isEqualTo("1_2_3");
        assertThat(SimpleJoin.of("", "", 1, "").onlyNonEmpty().join("_")).isEqualTo("1");
    }
}
