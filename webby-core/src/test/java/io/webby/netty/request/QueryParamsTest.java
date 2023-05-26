package io.webby.netty.request;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.ConversionError;
import io.webby.url.convert.IntConverter;
import io.webby.url.convert.StringConstraint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryParamsTest {
    @Test
    public void empty_uri() {
        QueryParams params = newQueryParamsWithoutConstraints("");
        assertThat(params.path()).isEqualTo("");
        assertThat(params.query()).isEqualTo("");
        assertParams(params, Map.of());
    }

    @Test
    public void empty_query() {
        QueryParams params = newQueryParamsWithoutConstraints("/foo");
        assertThat(params.path()).isEqualTo("/foo");
        assertThat(params.query()).isEqualTo("");
        assertParams(params, Map.of());
    }

    @Test
    public void empty_path() {
        QueryParams params = newQueryParamsWithoutConstraints("?foo=bar");
        assertThat(params.path()).isEqualTo("");
        assertThat(params.query()).isEqualTo("foo=bar");
        assertParams(params, Map.of("foo", List.of("bar")));
    }

    @Test
    public void simple_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=bar&baz=");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("foo=bar&baz=");
        assertParams(params, Map.of("foo", List.of("bar"), "baz", List.of("")));
    }

    @Test
    public void duplicate_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=bar&foo=");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("foo=bar&foo=");
        assertParams(params, Map.of("foo", List.of("bar", "")));
    }

    @Test
    public void duplicate_params_duplicate_values() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo=&foo=&foo=");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("foo=&foo=&foo=");
        assertParams(params, Map.of("foo", List.of("", "", "")));
    }

    @Test
    public void invalid_uri_missing_equals() {
        QueryParams params = newQueryParamsWithoutConstraints("/?foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("foo");
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_only() {
        QueryParams params = newQueryParamsWithoutConstraints("/?=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("=foo");
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_with_other_params() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=0&=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=0&=foo");
        assertParams(params, Map.of("x", List.of("0"), "foo", List.of("")));
    }

    @Test
    public void invalid_uri_empty_name_repeated() {
        QueryParams params = newQueryParamsWithoutConstraints("/?=foo&=");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("=foo&=");
        assertParams(params, Map.of("foo", List.of("")));
    }

    @Test
    public void int_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1&y=-1&z=&w=foo");
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertThat(params.getInt("x", 0)).isEqualTo(1);
        assertThat(params.getInt("y", 0)).isEqualTo(-1);
        assertThat(params.getInt("z", 0)).isEqualTo(0);
        assertThat(params.getInt("w", 0)).isEqualTo(0);
        assertThat(params.getInt("foobar", 0)).isEqualTo(0);
    }

    @Test
    public void long_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1&y=-1&z=&w=foo");
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertThat(params.getLong("x", 0)).isEqualTo(1);
        assertThat(params.getLong("y", 0)).isEqualTo(-1);
        assertThat(params.getLong("z", 0)).isEqualTo(0);
        assertThat(params.getLong("w", 0)).isEqualTo(0);
        assertThat(params.getLong("foobar", 0)).isEqualTo(0);
    }

    @Test
    public void byte_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=-1&z=&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1&y=-1&z=&w=foo");
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        byte zero = (byte) 0;
        assertThat(params.getByte("x", zero)).isEqualTo(1);
        assertThat(params.getByte("y", zero)).isEqualTo(-1);
        assertThat(params.getByte("z", zero)).isEqualTo(0);
        assertThat(params.getByte("w", zero)).isEqualTo(0);
        assertThat(params.getByte("foobar", zero)).isEqualTo(0);
    }

    @Test
    public void float_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1.0&y=-1&z=&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1.0&y=-1&z=&w=foo");
        assertParams(params, Map.of("x", List.of("1.0"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertThat(params.getFloat("x", 0)).isEqualTo(1.0f);
        assertThat(params.getFloat("y", 0)).isEqualTo(-1f);
        assertThat(params.getFloat("z", 0)).isEqualTo(0);
        assertThat(params.getFloat("w", 0)).isEqualTo(0);
        assertThat(params.getFloat("foobar", 0)).isEqualTo(0);
    }

    @Test
    public void double_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1.0&y=-1&z=&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1.0&y=-1&z=&w=foo");
        assertParams(params, Map.of("x", List.of("1.0"), "y", List.of("-1"), "z", List.of(""), "w", List.of("foo")));

        assertThat(params.getDouble("x", 0)).isEqualTo(1.0);
        assertThat(params.getDouble("y", 0)).isEqualTo(-1);
        assertThat(params.getDouble("z", 0)).isEqualTo(0);
        assertThat(params.getDouble("w", 0)).isEqualTo(0);
        assertThat(params.getDouble("foobar", 0)).isEqualTo(0);
    }

    @Test
    public void value_too_big() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=123456789012345678901234567890");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=123456789012345678901234567890");
        assertParams(params, Map.of("x", List.of("123456789012345678901234567890")));

        assertThat(params.getInt("x", 0)).isEqualTo(0);
        assertThat(params.getLong("x", 0)).isEqualTo(0);
        assertThat(params.getByte("x", (byte) 0)).isEqualTo(0);
        assertThat(params.getFloat("x", 0)).isWithin(1).of(1.2345679E29f);
        assertThat(params.getDouble("x", 0)).isWithin(1).of(1.2345678901234568E29);
    }

    @Test
    public void bool_value() {
        QueryParams params = newQueryParamsWithoutConstraints("/?x=1&y=true&z=false&w=foo");
        assertThat(params.path()).isEqualTo("/");
        assertThat(params.query()).isEqualTo("x=1&y=true&z=false&w=foo");
        assertParams(params, Map.of("x", List.of("1"), "y", List.of("true"), "z", List.of("false"), "w", List.of("foo")));

        assertThat(params.getBool("x")).isFalse();
        assertThat(params.getBool("x", false)).isFalse();
        assertThat(params.getBool("x", true)).isFalse();
        assertThat(params.getBoolOrTrue("x")).isFalse();

        assertThat(params.getBool("y")).isTrue();
        assertThat(params.getBool("y", false)).isTrue();
        assertThat(params.getBool("y", true)).isTrue();
        assertThat(params.getBoolOrTrue("y")).isTrue();

        assertThat(params.getBool("z")).isFalse();
        assertThat(params.getBool("z", true)).isFalse();
        assertThat(params.getBool("z", true)).isFalse();
        assertThat(params.getBoolOrTrue("z")).isFalse();

        assertThat(params.getBool("w")).isFalse();
        assertThat(params.getBool("w", false)).isFalse();
        assertThat(params.getBool("w", true)).isFalse();
        assertThat(params.getBoolOrTrue("w")).isFalse();

        assertThat(params.getBool("foobar")).isFalse();
        assertThat(params.getBool("foobar", false)).isFalse();
        assertThat(params.getBool("foobar", true)).isTrue();
        assertThat(params.getBoolOrTrue("foobar")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MIN_VALUE})
    public void int_constraint_common(int min) {
        IntConverter converter = new IntConverter(min, 1000);

        QueryParams params1 = newQueryParams("/?x=999", Map.of("x", converter));
        assertThat(params1.getInt("x", -1)).isEqualTo(999);
        assertThat(params1.<Integer>getConvertedOrDie("x")).isEqualTo(999);

        QueryParams params2 = newQueryParams("/?y=0", Map.of("x", converter));
        assertThat(params2.getInt("x", -1)).isEqualTo(-1);
        assertThat(params2.<Object>getConvertedIfExists("x")).isNull();
        assertThrows(NullPointerException.class, () -> params2.getConvertedOrDie("x"));

        QueryParams params3 = newQueryParams("/?x=foo", Map.of("x", converter));
        assertThat(params3.getInt("x", -1)).isEqualTo(-1);
        assertConversionError(() -> params3.getConvertedOrDie("x"), "[x]: Malformed integer: `foo`");

        QueryParams params4 = newQueryParams("/?x=", Map.of("x", converter));
        assertThat(params4.getInt("x", -1)).isEqualTo(-1);
        assertConversionError(() -> params4.getConvertedOrDie("x"), "[x]: Malformed integer: ``");
    }

    @Test
    public void int_constraint_any() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.ANY));
        assertThat(params1.getInt("x", -1)).isEqualTo(0);
        assertThat(params1.<Integer>getConvertedOrDie("x")).isEqualTo(0);

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.ANY));
        assertThat(params2.getInt("x", -1)).isEqualTo(-11);
        assertThat(params2.<Integer>getConvertedOrDie("x")).isEqualTo(-11);

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.ANY));
        assertThat(params3.getInt("x", -1)).isEqualTo(123);
        assertThat(params3.<Integer>getConvertedOrDie("x")).isEqualTo(123);
    }

    @Test
    public void int_constraint_positive() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.POSITIVE));
        assertThat(params1.getInt("x", -1)).isEqualTo(0);
        assertConversionError(() -> params1.getConvertedOrDie("x"), "[x]: Value `0` is out of bounds: [1, 2147483647]");

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.POSITIVE));
        assertThat(params2.getInt("x", -1)).isEqualTo(-11);
        assertConversionError(() -> params2.getConvertedOrDie("x"), "[x]: Value `-11` is out of bounds: [1, 2147483647]");

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.POSITIVE));
        assertThat(params3.getInt("x", -1)).isEqualTo(123);
        assertThat(params3.<Integer>getConvertedOrDie("x")).isEqualTo(123);
    }

    @Test
    public void int_constraint_non_negative() {
        QueryParams params1 = newQueryParams("/?x=0", Map.of("x", IntConverter.NON_NEGATIVE));
        assertThat(params1.getInt("x", -1)).isEqualTo(0);
        assertThat(params1.<Integer>getConvertedOrDie("x")).isEqualTo(0);

        QueryParams params2 = newQueryParams("/?x=-11", Map.of("x", IntConverter.NON_NEGATIVE));
        assertThat(params2.getInt("x", -1)).isEqualTo(-11);
        assertConversionError(() -> params2.getConvertedOrDie("x"), "[x]: Value `-11` is out of bounds: [0, 2147483647]");

        QueryParams params3 = newQueryParams("/?x=123", Map.of("x", IntConverter.NON_NEGATIVE));
        assertThat(params3.getInt("x", -1)).isEqualTo(123);
        assertThat(params3.<Integer>getConvertedOrDie("x")).isEqualTo(123);
    }

    @Test
    public void string_constraint_limited() {
        QueryParams params1 = newQueryParams("/?s=12345678", Map.of("s", new StringConstraint(8)));
        assertThat(params1.getOrNull("s")).isEqualTo("12345678");
        assertThat(params1.<String>getConvertedOrDie("s")).isEqualTo("12345678");

        QueryParams params2 = newQueryParams("/?s=123456789", Map.of("s", new StringConstraint(8)));
        assertThat(params2.getOrNull("s")).isEqualTo("123456789");
        assertConversionError(() -> params2.getConvertedOrDie("s"), "[s]: The value exceeds max length 8");

        QueryParams params3 = newQueryParams("/?s=", Map.of("s", new StringConstraint(8)));
        assertThat(params3.getOrNull("s")).isEqualTo("");
        assertThat(params3.<String>getConvertedOrDie("s")).isEqualTo("");
    }

    @Test
    public void string_constraint_unlimited() {
        QueryParams params1 = newQueryParams("/?s=12345678", Map.of("s", StringConstraint.UNLIMITED));
        assertThat(params1.getOrNull("s")).isEqualTo("12345678");
        assertThat(params1.<String>getConvertedOrDie("s")).isEqualTo("12345678");

        QueryParams params2 = newQueryParams("/?s=123456789", Map.of("s", StringConstraint.UNLIMITED));
        assertThat(params2.getOrNull("s")).isEqualTo("123456789");
        assertThat(params2.<String>getConvertedOrDie("s")).isEqualTo("123456789");

        QueryParams params3 = newQueryParams("/?s=", Map.of("s", StringConstraint.UNLIMITED));
        assertThat(params3.getOrNull("s")).isEqualTo("");
        assertThat(params3.<String>getConvertedOrDie("s")).isEqualTo("");
    }

    private static void assertParams(@NotNull QueryParams params, @NotNull Map<String, List<String>> expected) {
        assertThat(params.size()).isEqualTo(expected.size());
        assertThat(params.isEmpty()).isEqualTo(expected.isEmpty());
        assertThat(params.getMap()).isEqualTo(expected);
        assertThat(params.keys()).isEqualTo(expected.keySet());
        assertThat(params.stream().collect(Collectors.toSet())).isEqualTo(expected.entrySet());
        assertThat(params.biStream().toMap()).isEqualTo(expected);
        for (Map.Entry<String, List<String>> entry : expected.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            assertThat(params.contains(key)).isTrue();
            assertThat(params.getOrNull(key)).isEqualTo(value.get(0));
            assertThat(params.getAll(key)).isEqualTo(value);

            String altKey = "anti-%s!".formatted(key);
            assertThat(params.contains(altKey)).isFalse();
            assertThat(params.getOrNull(altKey)).isNull();
            assertThat(params.getAll(altKey)).isEqualTo(List.of());
        }
    }

    private static void assertConversionError(@NotNull Executable executable, @NotNull String message) {
        ConversionError error = assertThrows(ConversionError.class, executable);
        assertThat(error.getMessage()).isEqualTo(message);
    }

    private static @NotNull QueryParams newQueryParams(@NotNull String uri, @NotNull Map<String, Constraint<?>> constraints) {
        return QueryParams.fromDecoder(new QueryStringDecoder(uri), constraints);
    }

    private static @NotNull QueryParams newQueryParamsWithoutConstraints(@NotNull String uri) {
        return newQueryParams(uri, Map.of());
    }
}
