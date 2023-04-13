package io.webby.orm.codegen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static io.webby.orm.arch.factory.TestingArch.buildTableArch;
import static io.webby.orm.testing.AssertSql.assertThatSql;

public class ValuesArrayMakerTest {
    @Test
    public void primitive_columns() {
        record Primitives(int id, int i, long l, byte b, short s, char ch, float f, double d, boolean bool) {}

        TableArch tableArch = buildTableArch(Primitives.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());
        assertThat(maker)
            .matchesInitValues("""
                $param.id(),
                $param.i(),
                $param.l(),
                $param.b(),
                $param.s(),
                null,
                $param.f(),
                $param.d(),
                $param.bool(),
                """)
            .matchesConvertValues("""
                CharacterJdbcAdapter.fillArrayValues($param.ch(), array, 5);
                """);
    }

    @Test
    public void wrappers_columns() {
        record Wrappers(Integer id, Integer i, Long l, Byte b, Short s, Character ch, Float f, Double d, Boolean bool) {}

        TableArch tableArch = buildTableArch(Wrappers.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());
        assertThat(maker)
            .matchesInitValues("""
                $param.id(),
                $param.i(),
                $param.l(),
                $param.b(),
                $param.s(),
                null,
                $param.f(),
                $param.d(),
                $param.bool(),
                """)
            .matchesConvertValues("""
                CharacterJdbcAdapter.fillArrayValues($param.ch(), array, 5);
                """);
    }

    @Test
    public void nullable_columns() {
        record Nested(int id, @Nullable String s) {}
        record NullableModel(@Nullable String id, @Nullable String str, @Nullable Integer i, @Nullable Nested nest) {}

        TableArch tableArch = buildTableArch(NullableModel.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());
        assertThat(maker)
            .matchesInitValues("""
                $param.id(),
                $param.str(),
                $param.i(),
                null,
                null,
                """)
            .matchesConvertValues("""
                NestedJdbcAdapter.ADAPTER.fillArrayValues($param.nest(), array, 3);
                """);
    }

    private static @NotNull ValuesArrayMakerSubject assertThat(@NotNull ValuesArrayMaker valuesArrayMaker) {
        return new ValuesArrayMakerSubject(valuesArrayMaker);
    }

    @CanIgnoreReturnValue
    private record ValuesArrayMakerSubject(@NotNull ValuesArrayMaker valuesArrayMaker) {
        public @NotNull ValuesArrayMakerSubject matchesInitValues(@NotNull String expected) {
            assertThatSql(valuesArrayMaker.makeInitValues().join()).matches(expected);
            return this;
        }

        public @NotNull ValuesArrayMakerSubject matchesConvertValues(@NotNull String expected) {
            assertThatSql(valuesArrayMaker.makeConvertValues().join()).matches(expected);
            return this;
        }
    }
}
