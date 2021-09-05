package io.webby.netty.request;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.ConversionError;
import io.webby.url.convert.IntConverter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class QueryParamsTest {
    @Test
    public void empty_uri() {
        QueryParams params = newQueryParamsWithoutConstraints("");
        assertEquals("", params.path());
        assertEquals("", params.query());
        assertParams(params, Map.of());
    }

    @Test
    public void empty_query() {
        QueryParams params = newQueryParamsWithoutConstraints("/foo");
        assertEquals("/foo", params.path());
        assertEquals("", params.query());
        assertParams(params, Map.of());
    }

    @Test
    public void empty_path() {
        QueryParams params = newQueryParamsWithoutConstraints("?foo=bar");
        assertEquals("", params.path());
        assertEquals("foo=bar", params.query());
        assertParams(params, Map.of("foo", List.of("bar")));
    }

    @Test
    public void simple_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=bar&baz=");
        assertEquals("/", params.path());
        assertEquals("foo=bar&baz=", params.query());
        assertParams(params, Map.of("foo", List.of("bar"), "baz", List.of("")));
    }

    @Test
    public void duplicate_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=bar&foo=");
        assertEquals("/", params.path());
        assertEquals("foo=bar&foo=", params.query());
        assertParams(params, Map.of("foo", List.of("bar", "")));
    }

    @Test
    public void duplicate_params_duplicate_values() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=&foo=&foo=");
        assertEquals("/", params.path());
        assertEquals("foo=&foo=&foo=", params.query());
        assertParams(params, Map.of("foo", List.of("", "", "")));
    }

    @Test
    public void invalid_uri_missing_equals() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo");
        assertEquals("/", params.path());
        assertEquals("foo", params.query());
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_only() {
        QueryParams params = newQueryParamsWithoutConstraints("/?=foo");
        assertEquals("/", params.path());
        assertEquals("=foo", params.query());
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_with_other_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=0&=foo");
        assertEquals("/", params.path());
        assertEquals("x=0&=foo", params.query());
        assertParams(params, Map.of("x", List.of("0"), "foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_repeated() {
        QueryParams params = newQueryParamsWithoutConstraints("/?=foo&=");
        assertEquals("/", params.path());
        assertEquals("=foo&=", params.query());
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void int_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1&y=-1&z=&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertEquals(1, params.getInt("x", 0));
        assertEquals(-1, params.getInt("y", 0));
        assertEquals(0, params.getInt("z", 0));
        assertEquals(0, params.getInt("w", 0));
        assertEquals(0, params.getInt("foobar", 0));
    }

    @Test
    public void long_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1&y=-1&z=&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertEquals(1, params.getLong("x", 0));
        assertEquals(-1, params.getLong("y", 0));
        assertEquals(0, params.getLong("z", 0));
        assertEquals(0, params.getLong("w", 0));
        assertEquals(0, params.getLong("foobar", 0));
    }

    @Test
    public void byte_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1&y=-1&z=&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        byte zero = (byte) 0;
        assertEquals(1, params.getByte("x", zero));
        assertEquals(-1, params.getByte("y", zero));
        assertEquals(0, params.getByte("z", zero));
        assertEquals(0, params.getByte("w", zero));
        assertEquals(0, params.getByte("foobar", zero));
    }

    @Test
    public void float_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1.0&y=-1&z=&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1.0&y=-1&z=&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1.0"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertEquals(1.0, params.getFloat("x", 0));
        assertEquals(-1, params.getFloat("y", 0));
        assertEquals(0, params.getFloat("z", 0));
        assertEquals(0, params.getFloat("w", 0));
        assertEquals(0, params.getFloat("foobar", 0));
    }

    @Test
    public void double_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1.0&y=-1&z=&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1.0&y=-1&z=&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1.0"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertEquals(1.0, params.getDouble("x", 0));
        assertEquals(-1, params.getDouble("y", 0));
        assertEquals(0, params.getDouble("z", 0));
        assertEquals(0, params.getDouble("w", 0));
        assertEquals(0, params.getDouble("foobar", 0));
    }

    @Test
    public void value_too_big() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=123456789012345678901234567890");
        assertEquals("/", params.path());
        assertEquals("x=123456789012345678901234567890", params.query());
        assertParams(params, Map.of("x", List.of("123456789012345678901234567890")));

        assertEquals(0, params.getInt("x", 0));
        assertEquals(0, params.getLong("x", 0));
        assertEquals(0, params.getByte("x", (byte) 0));
        assertThat(params.getFloat("x", 0)).isWithin(1).of(1.2345679E29f);
        assertThat(params.getDouble("x", 0)).isWithin(1).of(1.2345678901234568E29);
    }

    @Test
    public void bool_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=true&z=false&w=foo");
        assertEquals("/", params.path());
        assertEquals("x=1&y=true&z=false&w=foo", params.query());
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("true"), "z", List.of("false"), "w", List.of("foo")));

        assertFalse(params.getBool("x"));
        assertFalse(params.getBool("x", false));
        assertFalse(params.getBool("x", true));
        assertFalse(params.getBoolOrTrue("x"));

        assertTrue(params.getBool("y"));
        assertTrue(params.getBool("y", false));
        assertTrue(params.getBool("y", true));
        assertTrue(params.getBoolOrTrue("y"));

        assertFalse(params.getBool("z"));
        assertFalse(params.getBool("z", true));
        assertFalse(params.getBool("z", true));
        assertFalse(params.getBoolOrTrue("z"));

        assertFalse(params.getBool("w"));
        assertFalse(params.getBool("w", false));
        assertFalse(params.getBool("w", true));
        assertFalse(params.getBoolOrTrue("w"));

        assertFalse(params.getBool("foobar"));
        assertFalse(params.getBool("foobar", false));
        assertTrue(params.getBool("foobar", true));
        assertTrue(params.getBoolOrTrue("foobar"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MIN_VALUE})
    public void int_constraint_common(int min) {
        IntConverter converter = new IntConverter(min, 1000);

        QueryParams params1 = newQueryParams("/?x=999", Map.of("x", converter));
        assertEquals(999, params1.getInt("x", -1));
        assertEquals(999, (Integer) params1.getConvertedOrNull("x"));

        QueryParams params2 = newQueryParams("/?y=0", Map.of("x", converter));
        assertEquals(-1, params2.getInt("x", -1));
        assertNull(params2.getConvertedOrNull("x"));

        QueryParams params3 = newQueryParams("/?x=foo", Map.of("x", converter));
        assertEquals(-1, params3.getInt("x", -1));
        assertConversionError(() -> params3.getConvertedOrNull("x"), "[x]: Malformed integer: `foo`");

        QueryParams params4 = newQueryParams("/?x=", Map.of("x", converter));
        assertEquals(-1, params4.getInt("x", -1));
        assertConversionError(() -> params4.getConvertedOrNull("x"), "[x]: Malformed integer: ``");
    }

    @Test
    public void int_constraint_any() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.ANY));
        assertEquals(0, params1.getInt("x", -1));
        assertEquals(0, (Integer) params1.getConvertedOrNull("x"));

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.ANY));
        assertEquals(-11, params2.getInt("x", -1));
        assertEquals(-11, (Integer) params2.getConvertedOrNull("x"));

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.ANY));
        assertEquals(123, params3.getInt("x", -1));
        assertEquals(123, (Integer) params3.getConvertedOrNull("x"));
    }

    @Test
    public void int_constraint_positive() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.POSITIVE));
        assertEquals(0, params1.getInt("x", -1));
        assertConversionError(() -> params1.getConvertedOrNull("x"), "[x]: Value `0` is out of bounds: [1, 2147483647]");

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.POSITIVE));
        assertEquals(-11, params2.getInt("x", -1));
        assertConversionError(() -> params2.getConvertedOrNull("x"), "[x]: Value `-11` is out of bounds: [1, 2147483647]");

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.POSITIVE));
        assertEquals(123, params3.getInt("x", -1));
        assertEquals(123, (Integer) params3.getConvertedOrNull("x"));
    }

    @Test
    public void int_constraint_non_negative() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.NON_NEGATIVE));
        assertEquals(0, params1.getInt("x", -1));
        assertEquals(0, (Integer) params1.getConvertedOrNull("x"));

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.NON_NEGATIVE));
        assertEquals(-11, params2.getInt("x", -1));
        assertConversionError(() -> params2.getConvertedOrNull("x"), "[x]: Value `-11` is out of bounds: [0, 2147483647]");

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.NON_NEGATIVE));
        assertEquals(123, params3.getInt("x", -1));
        assertEquals(123, (Integer) params3.getConvertedOrNull("x"));
    }

    private static void assertParams(@NotNull QueryParams params, @NotNull Map<String, List<String>> expected) {
        assertEquals(expected.size(), params.size());
        assertEquals(expected.isEmpty(), params.isEmpty());
        assertEquals(expected, params.getMap());
        assertEquals(expected.keySet(), params.keys());
        assertEquals(expected.entrySet(), params.stream().collect(Collectors.toSet()));
        assertEquals(expected, params.biStream().toMap());
        for (Map.Entry<String, List<String>> entry : expected.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            assertTrue(params.contains(key));
            assertEquals(value.get(0), params.getOrNull(key));
            assertEquals(value, params.getAll(key));

            String altKey = "anti-%s!".formatted(key);
            assertFalse(params.contains(altKey));
            assertNull(params.getOrNull(altKey));
            assertEquals(List.of(), params.getAll(altKey));
        }
    }

    private static void assertConversionError(@NotNull Executable executable, @NotNull String message) {
        ConversionError error = assertThrows(ConversionError.class, executable);
        assertEquals(message, error.getMessage());
    }

    private static @NotNull QueryParams newQueryParams(@NotNull String uri, @NotNull Map<String, Constraint<?>> constraints) {
        return QueryParams.fromDecoder(new QueryStringDecoder(uri), constraints);
    }

    private static @NotNull QueryParams newQueryParamsWithoutConstraints(@NotNull String uri) {
        return newQueryParams(uri, Map.of());
    }
}
