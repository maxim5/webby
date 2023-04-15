package io.webby.orm.testing;

import com.google.common.truth.Ordered;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.debug.DebugSql;
import io.webby.orm.api.query.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static io.webby.util.base.EasyCast.castAny;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertSql {
    public static @NotNull SqlSubject assertThatSql(@NotNull String query) {
        return new SqlSubject(query);
    }

    public static @NotNull ArgsSubject assertThat(@NotNull Args args) {
        return new ArgsSubject(args);
    }

    public static @NotNull RepresentableSubject assertThat(@NotNull Representable repr) {
        return new RepresentableSubject(repr);
    }

    public static @NotNull UnitSubject assertThat(@NotNull Unit unit) {
        return new UnitSubject(unit);
    }

    public static @NotNull TermSubject assertTerm(@NotNull Term term) {
        return new TermSubject(term);
    }

    public static @NotNull RowsSubject assertRows(@NotNull List<DebugSql.Row> rows) {
        return new RowsSubject(rows);
    }

    public static void assertReprThrows(@NotNull Supplier<Representable> repr) {
        assertThrows(InvalidQueryException.class, repr::get);
    }

    @CanIgnoreReturnValue
    public record SqlSubject(@NotNull String query) {
        public @NotNull SqlSubject matches(@NotNull String expected) {
            Truth.assertThat(query.trim()).isEqualTo(expected.trim());
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record ArgsSubject(@NotNull Args args) {
        public @NotNull ArgsSubject containsArgsExactly(@NotNull Object @Nullable ... expected) {
            Truth.assertThat(args.asList()).containsExactly(expected);
            return this;
        }

        public @NotNull ArgsSubject containsNoArgs() {
            Truth.assertThat(args.asList()).isEmpty();
            return this;
        }

        public @NotNull ArgsSubject allArgsResolved() {
            Truth.assertThat(args.isAllResolved()).isTrue();
            return this;
        }

        public @NotNull ArgsSubject containsUnresolved() {
            Truth.assertThat(args.isAllResolved()).isFalse();
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record RepresentableSubject(@NotNull Representable repr) {
        public @NotNull RepresentableSubject matches(@NotNull String expected) {
            assertThatSql(repr.repr()).matches(expected);
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record UnitSubject(@NotNull Unit unit) {
        public @NotNull UnitSubject matches(@NotNull String expected) {
            assertThatSql(unit.repr()).matches(expected);
            return this;
        }

        public @NotNull UnitSubject containsArgsExactly(@NotNull Object @Nullable ... expected) {
            assertThat(unit.args()).containsArgsExactly(expected);
            return this;
        }

        public @NotNull UnitSubject containsNoArgs() {
            assertThat(unit.args()).containsNoArgs();
            return this;
        }

        public @NotNull UnitSubject allArgsResolved() {
            Truth.assertThat(unit.args().isAllResolved()).isTrue();
            return this;
        }

        public @NotNull UnitSubject containsUnresolved() {
            Truth.assertThat(unit.args().isAllResolved()).isFalse();
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record TermSubject(@NotNull Term term) {
        public @NotNull TermSubject matches(@NotNull String expected) {
            assertThat(term).matches(expected);
            return this;
        }

        public @NotNull TermSubject hasType(@NotNull TermType type) {
            Truth.assertThat(term.type()).isEqualTo(type);
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record RowsSubject(@NotNull List<DebugSql.Row> rows) {
        public @NotNull Ordered containsExactly(@NotNull Object[] ... expected) {
            List<List<Object>> values = rows.stream()
                .map(row -> row.values().stream().map(DebugSql.RowValue::value).map(RowsSubject::adjust).toList())
                .toList();
            List<List<Object>> expectedList = Arrays.stream(expected)
                .map(row -> Arrays.stream(row).map(RowsSubject::adjust).toList())
                .toList();
            return Truth.assertThat(values).containsExactlyElementsIn(expectedList);
        }

        private static @Nullable Object adjust(@Nullable Object val) {
            if (val instanceof Boolean) {
                return val == Boolean.TRUE ? 1 : 0;
            }
            if (val instanceof Number number && (long) (number.intValue()) == number.longValue() &&
                (val instanceof Long || val instanceof BigInteger)) {
                return number.intValue();
            }
            if (val instanceof BigDecimal decimal) {
                return decimal.doubleValue();
            }
            return val;
        }
    }

    public static <T> @Nullable T adjustType(@Nullable Object val) {
        return castAny(RowsSubject.adjust(val));
    }
}
