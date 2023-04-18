package io.webby.orm.codegen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.arch.model.TableArch;
import io.webby.util.base.EasyPrimitives.MutableBool;
import io.webby.util.base.EasyPrimitives.MutableInt;
import io.webby.util.base.EasyPrimitives.MutableLong;
import io.webby.util.base.EasyPrimitives.OptionalBool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    public void columns_with_adapters() {
        // Point?
        record Adapters(MutableInt i, MutableBool bool, MutableLong l, OptionalBool optional) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                null,
                null,
                null,
                $param.optional().ordinal(),
                """)
            .matchesConvertValues("""
                EasyPrimitives_MutableInt_JdbcAdapter.ADAPTER.fillArrayValues($param.i(), array, 0);
                EasyPrimitives_MutableBool_JdbcAdapter.ADAPTER.fillArrayValues($param.bool(), array, 1);
                EasyPrimitives_MutableLong_JdbcAdapter.ADAPTER.fillArrayValues($param.l(), array, 2);
                """);
    }

    @Test
    public void columns_with_nullable_adapters() {
        record Adapters(@Nullable MutableInt i,
                        @javax.annotation.Nullable MutableBool bool,
                        @org.checkerframework.checker.nullness.qual.Nullable MutableLong l,
                        @Sql(nullable = true) OptionalBool optional) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                null,
                null,
                null,
                $param.optional().ordinal(),
                """)
            .matchesConvertValues("""
                EasyPrimitives_MutableInt_JdbcAdapter.ADAPTER.fillArrayValues($param.i(), array, 0);
                Optional.ofNullable($param.bool()).ifPresent(bool ->\
                 EasyPrimitives_MutableBool_JdbcAdapter.ADAPTER.fillArrayValues(bool, array, 1));
                EasyPrimitives_MutableLong_JdbcAdapter.ADAPTER.fillArrayValues($param.l(), array, 2);
                """);
    }

    @Test
    public void nullable_columns() {
        record Nested(int id, @javax.annotation.Nullable String s) {}
        @SuppressWarnings("NullableProblems")
        record NullableModel(@javax.annotation.Nullable String id,
                             @javax.annotation.Nullable String str,
                             @javax.annotation.Nullable Integer i,
                             @javax.annotation.Nullable char ch,
                             @javax.annotation.Nullable Nested nest) {}

        TableArch tableArch = buildTableArch(NullableModel.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.id(),
                $param.str(),
                $param.i(),
                null,
                null,
                null,
                """)
            .matchesConvertValues("""
                CharacterJdbcAdapter.fillArrayValues($param.ch(), array, 3);
                Optional.ofNullable($param.nest()).ifPresent(nest ->\
                 NestedJdbcAdapter.ADAPTER.fillArrayValues(nest, array, 4));
                """);
    }

    @Test
    public void foreign_key_columns() {
        record User(int userId, String name) {}
        record Song(ForeignInt<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
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
