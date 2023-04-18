package io.webby.orm.arch.factory;

import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.arch.JdbcType;
import io.webby.util.base.EasyPrimitives.OptionalBool;
import io.webby.util.collect.Pair;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static io.webby.orm.arch.factory.TestingArch.FieldConstraints.*;
import static io.webby.orm.arch.factory.TestingArch.TableFieldsStatus.*;
import static io.webby.orm.arch.factory.TestingArch.assertThat;
import static io.webby.orm.arch.factory.TestingArch.buildTableArch;

public class ArchFactoryTest {
    /** Generic tables **/

    @Test
    void empty_table() {
        record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class));  // maybe should throw?
    }

    @Test
    void default_table() {
        record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("simple_user", "io.webby.orm.arch.factory", "SimpleUserTable")
            .hasModel("SimpleUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    @Test
    void renamed_model_table() {
        @Model(javaName = "MyUser") record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("my_user", "io.webby.orm.arch.factory", "MyUserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    @Test
    void renamed_all_table() {
        @Model(javaName = "MyUser", sqlName = "users", javaTableName = "UserTable") record SimpleUser(int foo) {}

        assertThat(buildTableArch(SimpleUser.class))
            .hasTableName("users", "io.webby.orm.arch.factory", "UserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    /** Single usual field **/

    @Test
    void single_field_int() {
        record User(int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_long() {
        record User(long foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo()")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_boolean() {
        record User(boolean foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(boolean.class, "foo()")
            .isSingleColumn("foo", JdbcType.Boolean)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_string() {
        record User(String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_integer() {
        record User(Integer foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Integer.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_char() {
        record User(char foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(char.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveDefault()
            .isAdapterSupportedType()
            .usesAdapter("CharacterJdbcAdapter");
    }

    @Test
    void single_field_int_renamed() {
        record User(@Sql("bar") int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("bar", JdbcType.Int)
            .doesNotHaveDefault()
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_enum() {
        record User(OptionalBool bool) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("bool")
            .isFromTable("user")
            .hasInJava(OptionalBool.class, "bool()")
            .isSingleColumn("bool", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveAnyDefaults()
            .isMapperSupportedType();
    }

    @Test
    void single_field_complex() {
        record Tuple(int a, String b) {}
        record User(Tuple foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Tuple.class, "foo()")
            .hasColumns(Pair.of("foo_a", JdbcType.Int), Pair.of("foo_b", JdbcType.String))
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveAnyDefaults()
            .isAdapterSupportedType()
            .usesAdapter("TupleJdbcAdapter.ADAPTER");
    }

    @Test
    void single_field_complex_standard_point() {
        record User(Point point) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("point")
            .isFromTable("user")
            .hasInJava(Point.class, "point()")
            .hasColumns(Pair.of("point_x", JdbcType.Int), Pair.of("point_y", JdbcType.Int))
            .hasConstraints(USUAL_NOT_NULL)
            .doesNotHaveAnyDefaults()
            .isAdapterSupportedType()
            .usesAdapter("PointJdbcAdapter.ADAPTER");
    }

    @Test
    void single_field_optional() {
        record User(Optional<Integer> opt) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("opt")
            .isFromTable("user")
            .hasInJava(Optional.class, "opt()")
            .isSingleColumn("opt", JdbcType.Int)
            .hasConstraints(USUAL_NULLABLE)
            .doesNotHaveAnyDefaults()
            .isMapperSupportedType();
    }

    /** Single primary key field **/

    @Test
    void single_field_int_pk() {
        record User(int userId) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(int.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_pk_just_id() {
        record User(int id) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("id")
            .isFromTable("user")
            .hasInJava(int.class, "id()")
            .isSingleColumn("id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_long_pk() {
        record User(long userId) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY_LONG).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(long.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Long)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_string_pk() {
        record User(String userId) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(String.class, "userId()")
            .isSingleColumn("user_id", JdbcType.String)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_integer_pk() {
        record User(Integer userId) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(Integer.class, "userId()")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_pk_annotated() {
        record User(@Sql(primary = true) int userKey) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(int.class, "userKey()")
            .isSingleColumn("user_key", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .doesNotHaveDefault()
            .isNativelySupportedType();
    }

    /** Single foreign key field **/

    @Test
    void single_field_int_fk() {
        record Type(int id) {}
        record User(ForeignInt<Type> type) {}

        assertThat(buildTableArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignInt.class, "type()")
            .isSingleColumn("type_id", JdbcType.Int)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .doesNotHaveDefault();
    }

    @Test
    void single_field_long_fk() {
        record Type(long typeId) {}
        record User(ForeignLong<Type> type) {}

        assertThat(buildTableArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignLong.class, "type()")
            .isSingleColumn("type_id", JdbcType.Long)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .doesNotHaveDefault();
    }

    @Test
    void single_field_string_fk() {
        record Type(String typeId) {}
        record User(ForeignObj<String, Type> type) {}

        assertThat(buildTableArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignObj.class, "type()")
            .isSingleColumn("type_id", JdbcType.String)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .doesNotHaveDefault();
    }

    /** Single field with default **/

    @Test
    void single_field_int_with_default() {
        record User(@Sql(defaults = "0") int foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo()")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .hasDefault("0")
            .isNativelySupportedType();
    }

    @Test
    void single_field_long_with_default() {
        record User(@Sql(defaults = "-1") long foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo()")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(USUAL_NOT_NULL)
            .hasDefault("-1")
            .isNativelySupportedType();
    }

    @Test
    void single_field_string_with_default() {
        record User(@Sql(defaults = "") String foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo()")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(USUAL_NOT_NULL)
            .hasDefault("")
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_pk_annotated_with_default() {
        record User(@Sql(primary = true, defaults = "-1") int userKey) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(int.class, "userKey()")
            .isSingleColumn("user_key", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .hasDefault("-1")
            .isNativelySupportedType();
    }

    @Test
    void single_field_complex_with_default() {
        record Tuple(int a, String b) {}
        record User(@Sql(defaults = {"0", ""}) Tuple foo) {}

        assertThat(buildTableArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Tuple.class, "foo()")
            .hasColumns(Pair.of("foo_a", JdbcType.Int), Pair.of("foo_b", JdbcType.String))
            .hasConstraints(USUAL_NOT_NULL)
            .hasDefault("foo_a", "0")
            .hasDefault("foo_b", "")
            .isAdapterSupportedType()
            .usesAdapter("TupleJdbcAdapter.ADAPTER");
    }
}
