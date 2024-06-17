package io.webby.orm.codegen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.orm.api.Engine;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.arch.model.TableArch;
import io.webby.util.base.EasyWrappers.MutableBool;
import io.webby.util.base.EasyWrappers.MutableInt;
import io.webby.util.base.EasyWrappers.MutableLong;
import io.webby.util.base.EasyWrappers.OptionalBool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.webby.orm.arch.factory.TestingArch.buildTableArch;
import static io.webby.orm.codegen.AssertSnippet.assertThatJava;

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
    public void enum_columns() {
        record Enums(Engine engine, OptionalBool bool) {}

        TableArch tableArch = buildTableArch(Enums.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.engine().ordinal(),
                $param.bool().ordinal(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void columns_with_mappers() {
        record Mappers(Optional<String> str) {}

        TableArch tableArch = buildTableArch(Mappers.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.str().orElse(null),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void columns_with_adapters() {
        record Adapters(MutableInt i, MutableBool bool, MutableLong l, java.awt.Point point) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                null,
                null,
                null,
                null,
                null,
                """)
            .matchesConvertValues("""
                EasyWrappers_MutableInt_JdbcAdapter.ADAPTER.fillArrayValues($param.i(), array, 0);
                EasyWrappers_MutableBool_JdbcAdapter.ADAPTER.fillArrayValues($param.bool(), array, 1);
                EasyWrappers_MutableLong_JdbcAdapter.ADAPTER.fillArrayValues($param.l(), array, 2);
                PointJdbcAdapter.ADAPTER.fillArrayValues($param.point(), array, 3);
                """);
    }

    @Test
    public void columns_with_nullable_adapters() {
        record Adapters(@Nullable MutableInt i,
                        @javax.annotation.Nullable MutableBool bool,
                        @org.checkerframework.checker.nullness.qual.Nullable MutableLong l,
                        @Sql(nullable = true) java.awt.Point point) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                null,
                null,
                null,
                null,
                null,
                """)
            .matchesConvertValues("""
                EasyWrappers_MutableInt_JdbcAdapter.ADAPTER.fillArrayValues($param.i(), array, 0);
                Optional.ofNullable($param.bool()).ifPresent(bool ->\
                 EasyWrappers_MutableBool_JdbcAdapter.ADAPTER.fillArrayValues(bool, array, 1));
                EasyWrappers_MutableLong_JdbcAdapter.ADAPTER.fillArrayValues($param.l(), array, 2);
                Optional.ofNullable($param.point()).ifPresent(point ->\
                 PointJdbcAdapter.ADAPTER.fillArrayValues(point, array, 3));
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
    public void foreign_int_column() {
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

    @Test
    public void foreign_long_column() {
        record User(long userId, String name) {}
        record Song(ForeignLong<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_string_column() {
        record User(String userId, int age) {}
        record Song(ForeignObj<String, User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_int_column_two_levels() {
        record User(int userId, String name) {}
        record Song(int songId, ForeignInt<User> author) {}
        record Single(ForeignInt<Song> hitSong) {}

        TableArch tableArch = buildTableArch(Single.class, List.of(Song.class, User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.hitSong().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_int_column_nullable() {
        record User(int userId, String name) {}
        record Song(@Sql.Null ForeignInt<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_long_column_nullable() {
        record User(long userId, String name) {}
        record Song(@Sql.Null ForeignLong<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_string_column_nullable() {
        record User(String userId, int age) {}
        record Song(@Sql.Null ForeignObj<String, User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.author().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @Test
    public void foreign_int_column_two_levels_nullable() {
        record User(int userId, String name) {}
        record Song(int songId, @Sql.Null ForeignInt<User> author) {}
        record Single(@Sql.Null ForeignInt<Song> hitSong) {}

        TableArch tableArch = buildTableArch(Single.class, List.of(Song.class, User.class));
        ValuesArrayMaker maker = new ValuesArrayMaker("$param", tableArch.fields());

        assertThat(maker)
            .matchesInitValues("""
                $param.hitSong().getFk(),
                """)
            .matchesConvertValues("""
                """);
    }

    @CheckReturnValue
    private static @NotNull ValuesArrayMakerSubject assertThat(@NotNull ValuesArrayMaker valuesArrayMaker) {
        return new ValuesArrayMakerSubject(valuesArrayMaker);
    }

    @CanIgnoreReturnValue
    private record ValuesArrayMakerSubject(@NotNull ValuesArrayMaker valuesArrayMaker) {
        public @NotNull ValuesArrayMakerSubject matchesInitValues(@NotNull String expected) {
            assertThatJava(valuesArrayMaker.makeInitValues()).matches(expected);
            return this;
        }

        public @NotNull ValuesArrayMakerSubject matchesConvertValues(@NotNull String expected) {
            assertThatJava(valuesArrayMaker.makeConvertValues()).matches(expected);
            return this;
        }
    }
}
