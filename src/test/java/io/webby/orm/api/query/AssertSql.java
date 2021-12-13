package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertSql {
    public static void assertRepr(@NotNull Representable repr, @NotNull String expected) {
        assertEquals(expected, repr.repr());
    }

    public static void assertRepr(@NotNull Term term, @NotNull String expected, @NotNull TermType expectedType) {
        assertRepr(term, expected);
        assertEquals(expectedType, term.type());
    }

    public static void assertReprThrows(@NotNull Supplier<Representable> repr) {
        assertThrows(InvalidQueryException.class, repr::get);
    }
}
