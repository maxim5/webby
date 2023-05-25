package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class FastFormatTest {
    public static final Object NULL = null;

    @Test
    public void no_specs() {
        assertFormat("");
        assertFormat("foo");
        assertFormat("foo", 1);
        assertFormat("foo", 1, NULL);
    }

    @Test
    public void no_specs_escaped() {
        assertFormat("%%");
        assertFormat("%%%%");
        assertFormat("%% foo bar %% %%");
    }

    @Test
    public void one_str_simple() {
        assertFormat("%s", "foo");
        assertFormat("%s", NULL);
        assertFormat("foo %s", "bar");
        assertFormat("foo %s", 1);
        assertFormat("%s foo", 1);
        assertFormat("%s foo", NULL);
        assertFormat("%s foo %%", 1);
        assertFormat("%s foo %%", NULL);
        assertFormat("%s foo %% %%", 1);
        assertFormat("%s foo %% %%", NULL);
    }

    @Test
    public void one_int_simple() {
        assertFormat("%s", 111);
        assertFormat("foo %s", 222);
        assertFormat("%s foo", 333);
        assertFormat("%s foo %%", 444);
    }

    @Test
    public void one_long_simple() {
        assertFormat("%s", 111L);
        assertFormat("foo %s", 222L);
        assertFormat("%s foo", 333L);
        assertFormat("%s foo %%", 444L);
    }

    @Test
    public void one_boolean_simple() {
        assertFormat("%s", true);
        assertFormat("foo %s", false);
        assertFormat("%s foo", true);
        assertFormat("%s foo %%", false);
    }

    @Test
    public void two_str_simple() {
        assertFormat("%s%s", "foo", "bar");
        assertFormat("%s%s", NULL, "foo");
        assertFormat("%s%s", "foo", NULL);
        assertFormat("%s%s", NULL, NULL);
        assertFormat("foo %s bar %s", "foo", "bar");
        assertFormat("foo %s bar %s", NULL, "bar");
        assertFormat("foo %s bar %s", "foo", NULL);
        assertFormat("foo %s bar %s", NULL, NULL);
        assertFormat("foo %s bar %s %%%%", "foo", "bar");
    }

    private static void assertFormat(@NotNull String pattern, @Nullable Object arg) {
        String formatted = FastFormat.format(pattern, arg);
        String expected = pattern.formatted(arg);
        assertThat(formatted).isEqualTo(expected);
    }

    private static void assertFormat(@NotNull String pattern, int arg) {
        String formatted = FastFormat.format(pattern, arg);
        String expected = pattern.formatted(arg);
        assertThat(formatted).isEqualTo(expected);
    }

    private static void assertFormat(@NotNull String pattern, long arg) {
        String formatted = FastFormat.format(pattern, arg);
        String expected = pattern.formatted(arg);
        assertThat(formatted).isEqualTo(expected);
    }

    private static void assertFormat(@NotNull String pattern, boolean arg) {
        String formatted = FastFormat.format(pattern, arg);
        String expected = pattern.formatted(arg);
        assertThat(formatted).isEqualTo(expected);
    }

    private static void assertFormat(@NotNull String pattern, @Nullable Object @NotNull ... args) {
        String formatted = FastFormat.format(pattern, args);
        String expected = pattern.formatted(args);
        assertThat(formatted).isEqualTo(expected);
    }
}
