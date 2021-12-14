package io.webby.orm.testing;

import io.webby.orm.api.debug.DebugSql;
import io.webby.orm.api.query.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertSql {
    public static void assertRepr(@NotNull Representable repr, @NotNull String expected) {
        assertEquals(expected.trim(), repr.repr().trim());
    }

    public static void assertRepr(@NotNull Term term, @NotNull String expected, @NotNull TermType expectedType) {
        assertRepr(term, expected);
        assertEquals(expectedType, term.type());
    }

    public static void assertReprThrows(@NotNull Supplier<Representable> repr) {
        assertThrows(InvalidQueryException.class, repr::get);
    }

    public static void assertArgs(@NotNull ArgsHolder holder, @NotNull Object @Nullable ... expected) {
        assertThat(holder.args()).containsExactly(expected);
    }

    public static void assertNoArgs(@NotNull ArgsHolder holder) {
        assertArgs(holder);
    }

    public static void assertRows(@NotNull List<DebugSql.Row> result, @NotNull Object[] ... expected) {
        List<List<Object>> values = result.stream()
                .map(row -> row.values().stream().map(DebugSql.RowValue::value).map(AssertSql::adjust).toList())
                .toList();
        List<List<Object>> expectedList = Arrays.stream(expected)
                .map(row -> Arrays.stream(row).map(AssertSql::adjust).toList())
                .toList();
        assertThat(values).containsExactlyElementsIn(expectedList);
    }

    private static @Nullable Object adjust(@Nullable Object val) {
        if (val instanceof Boolean) {
            return val == Boolean.TRUE ? 1 : 0;
        }
        if (val instanceof Number number && (long) (number.intValue()) == number.longValue() &&
            (val instanceof Long || val instanceof BigInteger)) {
            return number.intValue();
        }
        return val;
    }
}
