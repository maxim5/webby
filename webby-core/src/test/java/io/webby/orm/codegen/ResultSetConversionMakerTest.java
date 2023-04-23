package io.webby.orm.codegen;

import io.webby.orm.api.Engine;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.testing.AssertSql;
import io.webby.orm.testing.AssertSql.SqlSubject;
import io.webby.util.base.EasyPrimitives.MutableBool;
import io.webby.util.base.EasyPrimitives.MutableInt;
import io.webby.util.base.EasyPrimitives.MutableLong;
import io.webby.util.base.EasyPrimitives.OptionalBool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.webby.orm.arch.factory.TestingArch.buildTableArch;

public class ResultSetConversionMakerTest {
    @Test
    public void primitive_columns() {
        record Primitives(int id, int i, long l, byte b, short s, char ch, float f, double d, boolean bool) {}

        TableArch tableArch = buildTableArch(Primitives.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            int id = $resultSet.getInt(++$index);
            int i = $resultSet.getInt(++$index);
            long l = $resultSet.getLong(++$index);
            byte b = $resultSet.getByte(++$index);
            short s = $resultSet.getShort(++$index);
            char ch = CharacterJdbcAdapter.createInstance($resultSet.getString(++$index));
            float f = $resultSet.getFloat(++$index);
            double d = $resultSet.getDouble(++$index);
            boolean bool = $resultSet.getBoolean(++$index);
            """);
    }

    @Test
    public void wrappers_columns() {
        record Wrappers(Integer id, Integer i, Long l, Byte b, Short s, Character ch, Float f, Double d, Boolean bool) {}

        TableArch tableArch = buildTableArch(Wrappers.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            Integer id = $resultSet.getInt(++$index);
            Integer i = $resultSet.getInt(++$index);
            Long l = $resultSet.getLong(++$index);
            Byte b = $resultSet.getByte(++$index);
            Short s = $resultSet.getShort(++$index);
            Character ch = CharacterJdbcAdapter.createInstance($resultSet.getString(++$index));
            Float f = $resultSet.getFloat(++$index);
            Double d = $resultSet.getDouble(++$index);
            Boolean bool = $resultSet.getBoolean(++$index);
            """);
    }

    @Test
    public void enum_columns() {
        record Enums(Engine engine, OptionalBool bool) {}

        TableArch tableArch = buildTableArch(Enums.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            Engine engine = Engine.values()[$resultSet.getInt(++$index)];
            EasyPrimitives.OptionalBool bool = EasyPrimitives.OptionalBool.values()[$resultSet.getInt(++$index)];
            """);
    }

    @Test
    public void columns_with_mappers() {
        record Mappers(Optional<String> str) {}

        TableArch tableArch = buildTableArch(Mappers.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            Optional str = Optional.ofNullable($resultSet.getString(++$index));
            """);
    }

    @Test
    public void columns_with_adapters() {
        record Adapters(MutableInt i, MutableBool bool, MutableLong l, java.awt.Point point) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            EasyPrimitives.MutableInt i = EasyPrimitives_MutableInt_JdbcAdapter.ADAPTER.createInstance($resultSet.getInt(++$index));
            EasyPrimitives.MutableBool bool = EasyPrimitives_MutableBool_JdbcAdapter.ADAPTER.createInstance($resultSet.getBoolean(++$index));
            EasyPrimitives.MutableLong l = EasyPrimitives_MutableLong_JdbcAdapter.ADAPTER.createInstance($resultSet.getLong(++$index));
            Point point = PointJdbcAdapter.ADAPTER.createInstance($resultSet.getInt(++$index), $resultSet.getInt(++$index));
            """);
    }

    @Test
    public void columns_with_nullable_adapters() {
        record Adapters(@Nullable MutableInt i,
                        @javax.annotation.Nullable MutableBool bool,
                        @org.checkerframework.checker.nullness.qual.Nullable MutableLong l,
                        @Sql(nullable = true) java.awt.Point point) {}

        TableArch tableArch = buildTableArch(Adapters.class);
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            EasyPrimitives.MutableInt i = EasyPrimitives_MutableInt_JdbcAdapter.ADAPTER.createInstance($resultSet.getInt(++$index));
            EasyPrimitives.MutableBool bool = EasyPrimitives_MutableBool_JdbcAdapter.ADAPTER.createInstance($resultSet.getBoolean(++$index));
            EasyPrimitives.MutableLong l = EasyPrimitives_MutableLong_JdbcAdapter.ADAPTER.createInstance($resultSet.getLong(++$index));
            Point point = PointJdbcAdapter.ADAPTER.createInstance($resultSet.getInt(++$index), $resultSet.getInt(++$index));
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
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            String id = $resultSet.getString(++$index);
            String str = $resultSet.getString(++$index);
            Integer i = $resultSet.getInt(++$index);
            char ch = CharacterJdbcAdapter.createInstance($resultSet.getString(++$index));
            Nested nest = NestedJdbcAdapter.ADAPTER.createInstance($resultSet.getInt(++$index), $resultSet.getString(++$index));
            """);
    }

    @Test
    public void foreign_int_column() {
        record User(int userId, String name) {}
        record Song(ForeignInt<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignInt author = switch ($follow) {
                case NO_FOLLOW -> ForeignInt.ofId($resultSet.getInt(++$index));
                case FOLLOW_ONE_LEVEL -> ForeignInt.ofEntity($resultSet.getInt(++$index), UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                case FOLLOW_ALL -> ForeignInt.ofEntity($resultSet.getInt(++$index), UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
            };
        """);
    }

    @Test
    public void foreign_long_column() {
        record User(long userId, String name) {}
        record Song(ForeignLong<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignLong author = switch ($follow) {
                case NO_FOLLOW -> ForeignLong.ofId($resultSet.getLong(++$index));
                case FOLLOW_ONE_LEVEL -> ForeignLong.ofEntity($resultSet.getLong(++$index), UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                case FOLLOW_ALL -> ForeignLong.ofEntity($resultSet.getLong(++$index), UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
            };
        """);
    }

    @Test
    public void foreign_string_column() {
        record User(String userId, int age) {}
        record Song(ForeignObj<String, User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignObj author = switch ($follow) {
                case NO_FOLLOW -> ForeignObj.ofId($resultSet.getString(++$index));
                case FOLLOW_ONE_LEVEL -> ForeignObj.ofEntity($resultSet.getString(++$index), UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                case FOLLOW_ALL -> ForeignObj.ofEntity($resultSet.getString(++$index), UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
            };
        """);
    }

    @Test
    public void foreign_int_column_two_levels() {
        record User(int userId, String name) {}
        record Song(int songId, ForeignInt<User> author) {}
        record Single(ForeignInt<Song> hitSong) {}

        TableArch tableArch = buildTableArch(Single.class, List.of(Song.class, User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignInt hitSong = switch ($follow) {
                case NO_FOLLOW -> ForeignInt.ofId($resultSet.getInt(++$index));
                case FOLLOW_ONE_LEVEL -> ForeignInt.ofEntity($resultSet.getInt(++$index), SongTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                case FOLLOW_ALL -> ForeignInt.ofEntity($resultSet.getInt(++$index), SongTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 4) - 4));
            };
        """);
    }

    @Test
    public void foreign_int_column_nullable() {
        record User(int userId, String name) {}
        record Song(@Sql.Null ForeignInt<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignInt author = switch ($follow) {
                case NO_FOLLOW -> ForeignInt.ofId($resultSet.getInt(++$index));
                case FOLLOW_ONE_LEVEL -> {
                    int _author = $resultSet.getInt(++$index);
                    if (_author == 0) {
                        $index += 2;
                        yield ForeignInt.empty();
                    } else {
                        yield ForeignInt.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                    }
                }
                case FOLLOW_ALL -> {
                    int _author = $resultSet.getInt(++$index);
                    if (_author == 0) {
                        $index += 2;
                        yield ForeignInt.empty();
                    } else {
                        yield ForeignInt.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
                    }
                }
            };
        """);
    }

    @Test
    public void foreign_long_column_nullable() {
        record User(long userId, String name) {}
        record Song(@Sql.Null ForeignLong<User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignLong author = switch ($follow) {
                case NO_FOLLOW -> ForeignLong.ofId($resultSet.getLong(++$index));
                case FOLLOW_ONE_LEVEL -> {
                    long _author = $resultSet.getLong(++$index);
                    if (_author == 0) {
                        $index += 2;
                        yield ForeignLong.empty();
                    } else {
                        yield ForeignLong.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                    }
                }
                case FOLLOW_ALL -> {
                    long _author = $resultSet.getLong(++$index);
                    if (_author == 0) {
                        $index += 2;
                        yield ForeignLong.empty();
                    } else {
                        yield ForeignLong.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
                    }
                }
            };
        """);
    }

    @Test
    public void foreign_string_column_nullable() {
        record User(String userId, int age) {}
        record Song(@Sql.Null ForeignObj<String, User> author) {}

        TableArch tableArch = buildTableArch(Song.class, List.of(User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignObj author = switch ($follow) {
                case NO_FOLLOW -> {
                    String _author = $resultSet.getString(++$index);
                    if (_author == null) {
                        /* no need to increment `$index` */;
                        yield ForeignObj.empty();
                    } else {
                        yield ForeignObj.ofId(_author);
                    }
                }
                case FOLLOW_ONE_LEVEL -> {
                    String _author = $resultSet.getString(++$index);
                    if (_author == null) {
                        $index += 2;
                        yield ForeignObj.empty();
                    } else {
                        yield ForeignObj.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                    }
                }
                case FOLLOW_ALL -> {
                    String _author = $resultSet.getString(++$index);
                    if (_author == null) {
                        $index += 2;
                        yield ForeignObj.empty();
                    } else {
                        yield ForeignObj.ofEntity(_author, UserTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 2) - 2));
                    }
                }
            };
        """);
    }

    @Test
    public void foreign_int_column_two_levels_nullable() {
        record User(int userId, String name) {}
        record Song(int songId, @Sql.Null ForeignInt<User> author) {}
        record Single(@Sql.Null ForeignInt<Song> hitSong) {}

        TableArch tableArch = buildTableArch(Single.class, List.of(Song.class, User.class));
        assertThatSql(new ResultSetConversionMaker("$resultSet", "$follow", "$index").make(tableArch)).matches("""
            ForeignInt hitSong = switch ($follow) {
                case NO_FOLLOW -> ForeignInt.ofId($resultSet.getInt(++$index));
                case FOLLOW_ONE_LEVEL -> {
                    int _hitSong = $resultSet.getInt(++$index);
                    if (_hitSong == 0) {
                        $index += 2;
                        yield ForeignInt.empty();
                    } else {
                        yield ForeignInt.ofEntity(_hitSong, SongTable.fromRow($resultSet, ReadFollow.NO_FOLLOW, ($index += 2) - 2));
                    }
                }
                case FOLLOW_ALL -> {
                    int _hitSong = $resultSet.getInt(++$index);
                    if (_hitSong == 0) {
                        $index += 4;
                        yield ForeignInt.empty();
                    } else {
                        yield ForeignInt.ofEntity(_hitSong, SongTable.fromRow($resultSet, ReadFollow.FOLLOW_ALL, ($index += 4) - 4));
                    }
                }
            };
        """);
    }

    private static @NotNull SqlSubject assertThatSql(@NotNull Snippet snippet) {
        return AssertSql.assertThatSql(snippet.join());
    }
}
