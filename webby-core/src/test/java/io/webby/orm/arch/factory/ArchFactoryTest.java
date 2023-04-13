package io.webby.orm.arch.factory;

import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.arch.JdbcType;
import io.webby.orm.arch.model.TableArch;
import io.webby.util.collect.ListBuilder;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.webby.orm.arch.factory.TestingArch.FieldConstraints.*;
import static io.webby.orm.arch.factory.TestingArch.TableFieldsStatus.*;
import static io.webby.orm.arch.factory.TestingArch.assertThat;

public class ArchFactoryTest {
    /** Generic tables **/

    @Test
    void empty_table() {
        record SimpleUser(int foo) {}

        assertThat(buildArch(SimpleUser.class));  // maybe should throw?
    }

    @Test
    void default_table() {
        record SimpleUser(int foo) {}

        assertThat(buildArch(SimpleUser.class))
            .hasTableName("simple_user", "io.webby.orm.arch.factory", "SimpleUserTable")
            .hasModel("SimpleUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    @Test
    void renamed_model_table() {
        @Model(javaName = "MyUser") record SimpleUser(int foo) {}

        assertThat(buildArch(SimpleUser.class))
            .hasTableName("my_user", "io.webby.orm.arch.factory", "MyUserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    @Test
    void renamed_all_table() {
        @Model(javaName = "MyUser", sqlName = "users", javaTableName = "UserTable") record SimpleUser(int foo) {}

        assertThat(buildArch(SimpleUser.class))
            .hasTableName("users", "io.webby.orm.arch.factory", "UserTable")
            .hasModel("MyUser", SimpleUser.class)
            .hasFields(HAS_NO_KEY_FIELDS);
    }

    /** Single usual field **/

    @Test
    void single_field_int() {
        record User(int foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_long() {
        record User(long foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(long.class, "foo")
            .isSingleColumn("foo", JdbcType.Long)
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_string() {
        record User(String foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(String.class, "foo")
            .isSingleColumn("foo", JdbcType.String)
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_integer() {
        record User(Integer foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Integer.class, "foo")
            .isSingleColumn("foo", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_renamed() {
        record User(@Sql("bar") int foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(int.class, "foo")
            .isSingleColumn("bar", JdbcType.Int)
            .hasConstraints(USUAL_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_complex() {
        record Tuple(int a, String b) {}
        record User(Tuple foo) {}

        assertThat(buildArch(User.class)).hasFields(HAS_NO_KEY_FIELDS).hasSingleFieldThat("foo")
            .isFromTable("user")
            .hasInJava(Tuple.class, "foo")
            .hasColumns(Pair.of("foo_a", JdbcType.Int), Pair.of("foo_b", JdbcType.String))
            .hasConstraints(USUAL_NOT_NULL)
            .isCustomSupportedType()
            .usesAdapter("TupleJdbcAdapter.ADAPTER");
    }

    /** Single primary key field **/

    @Test
    void single_field_int_pk() {
        record User(int userId) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(int.class, "userId")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_pk_just_id() {
        record User(int id) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("id")
            .isFromTable("user")
            .hasInJava(int.class, "id")
            .isSingleColumn("id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_long_pk() {
        record User(long userId) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY_LONG).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(long.class, "userId")
            .isSingleColumn("user_id", JdbcType.Long)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_string_pk() {
        record User(String userId) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(String.class, "userId")
            .isSingleColumn("user_id", JdbcType.String)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_integer_pk() {
        record User(Integer userId) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY).hasSingleFieldThat("userId")
            .isFromTable("user")
            .hasInJava(Integer.class, "userId")
            .isSingleColumn("user_id", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    @Test
    void single_field_int_pk_annotated() {
        record User(@Sql(primary = true) int userKey) {}

        assertThat(buildArch(User.class)).hasFields(HAS_PRIMARY_INT).hasSingleFieldThat("userKey")
            .isFromTable("user")
            .hasInJava(int.class, "userKey")
            .isSingleColumn("user_key", JdbcType.Int)
            .hasConstraints(PRIMARY_NOT_NULL)
            .isNativelySupportedType();
    }

    /** Single foreign key field **/

    @Test
    void single_field_int_fk() {
        record Type(int id) {}
        record User(ForeignInt<Type> type) {}

        assertThat(buildArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignInt.class, "type")
            .isSingleColumn("type_id", JdbcType.Int)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .isCustomSupportedType();
    }

    @Test
    void single_field_long_fk() {
        record Type(long typeId) {}
        record User(ForeignLong<Type> type) {}

        assertThat(buildArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignLong.class, "type")
            .isSingleColumn("type_id", JdbcType.Long)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .isCustomSupportedType();
    }

    @Test
    void single_field_string_fk() {
        record Type(String typeId) {}
        record User(ForeignObj<String, Type> type) {}

        assertThat(buildArch(User.class, List.of(Type.class))).hasFields(HAS_FOREIGN).hasSingleFieldThat("type")
            .isFromTable("user")
            .hasInJava(ForeignObj.class, "type")
            .isSingleColumn("type_id", JdbcType.String)
            .isForeign("type_id", "type")
            .hasConstraints(FOREIGN_KEY_NOT_NULL)
            .isCustomSupportedType();
    }

    /** Helpers **/

    private static @NotNull TableArch buildArch(@NotNull Class<?> model) {
        RunContext runContext = TestingArch.newRunContext(model);
        new ArchFactory(runContext).build();
        return runContext.tables().getTableOrDie(model);
    }

    private static @NotNull TableArch buildArch(@NotNull Class<?> model, @NotNull List<Class<?>> rest) {
        RunContext runContext = TestingArch.newRunContext(ListBuilder.concatOne(rest, model));
        new ArchFactory(runContext).build();
        return runContext.tables().getTableOrDie(model);
    }
}
