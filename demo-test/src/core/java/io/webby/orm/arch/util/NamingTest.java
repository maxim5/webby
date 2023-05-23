package io.webby.orm.arch.util;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.session.SessionModel;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserModel;
import io.webby.demo.model.NestedModel;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class NamingTest {
    @Test
    public void generatedSimpleJavaName_simple() {
        assertThat(Naming.generatedSimpleJavaName(UserModel.class)).isEqualTo("UserModel");
        assertThat(Naming.generatedSimpleJavaName(DefaultUser.class)).isEqualTo("DefaultUser");
        assertThat(Naming.generatedSimpleJavaName(SessionModel.class)).isEqualTo("SessionModel");
        assertThat(Naming.generatedSimpleJavaName(DefaultSession.class)).isEqualTo("DefaultSession");

        assertThat(Naming.generatedSimpleJavaName(NestedModel.class)).isEqualTo("NestedModel");
        assertThat(Naming.generatedSimpleJavaName(NestedModel.Level1.class)).isEqualTo("NestedModel_Level1");
        assertThat(Naming.generatedSimpleJavaName(NestedModel.Simple.class)).isEqualTo("NestedModel_Simple");
    }

    @Test
    public void shortCanonicalJavaName_simple() {
        assertThat(Naming.shortCanonicalJavaName(UserModel.class)).isEqualTo("UserModel");
        assertThat(Naming.shortCanonicalJavaName(DefaultUser.class)).isEqualTo("DefaultUser");
        assertThat(Naming.shortCanonicalJavaName(SessionModel.class)).isEqualTo("SessionModel");
        assertThat(Naming.shortCanonicalJavaName(DefaultSession.class)).isEqualTo("DefaultSession");

        assertThat(Naming.shortCanonicalJavaName(NestedModel.class)).isEqualTo("NestedModel");
        assertThat(Naming.shortCanonicalJavaName(NestedModel.Level1.class)).isEqualTo("NestedModel.Level1");
        assertThat(Naming.shortCanonicalJavaName(NestedModel.Simple.class)).isEqualTo("NestedModel.Simple");
    }

    @Test
    public void fieldSqlName_simple() {
        assertThat(Naming.fieldSqlName("Foo")).isEqualTo("foo");
        assertThat(Naming.fieldSqlName("_foo")).isEqualTo("foo");
        assertThat(Naming.fieldSqlName("Foo_")).isEqualTo("foo");
        assertThat(Naming.fieldSqlName("foo")).isEqualTo("foo");

        assertThat(Naming.fieldSqlName("FooBar")).isEqualTo("foo_bar");
        assertThat(Naming.fieldSqlName("fooBar")).isEqualTo("foo_bar");
        assertThat(Naming.fieldSqlName("Foo_Bar")).isEqualTo("foo_bar");
        assertThat(Naming.fieldSqlName("Foo_Bar__")).isEqualTo("foo_bar");

        assertThat(Naming.fieldSqlName("foo_bar")).isEqualTo("foo_bar");
        assertThat(Naming.fieldSqlName("foo__bar")).isEqualTo("foo_bar");
    }

    @Test
    public void modelSqlName_simple() {
        assertThat(Naming.modelSqlName("Foo")).isEqualTo("foo");
        assertThat(Naming.modelSqlName("_foo")).isEqualTo("foo");
        assertThat(Naming.modelSqlName("Foo_")).isEqualTo("foo");
        assertThat(Naming.modelSqlName("foo")).isEqualTo("foo");

        assertThat(Naming.modelSqlName("FooBar")).isEqualTo("foo_bar");
        assertThat(Naming.modelSqlName("fooBar")).isEqualTo("foo_bar");
        assertThat(Naming.modelSqlName("Foo_Bar")).isEqualTo("foo_bar");
        assertThat(Naming.modelSqlName("Foo_Bar__")).isEqualTo("foo_bar");

        assertThat(Naming.modelSqlName("foo_bar")).isEqualTo("foo_bar");
        assertThat(Naming.modelSqlName("foo__bar")).isEqualTo("foo_bar");
    }

    @Test
    public void concatSqlNames_simple() {
        assertThat(Naming.concatSqlNames("foo", "bar")).isEqualTo("foo_bar");
        assertThat(Naming.concatSqlNames("foo_", "_bar")).isEqualTo("foo_bar");
        assertThat(Naming.concatSqlNames("_foo_", "_bar_")).isEqualTo("foo_bar");
        assertThat(Naming.concatSqlNames("foo", "")).isEqualTo("foo_");
        assertThat(Naming.concatSqlNames("foo", "_")).isEqualTo("foo_");
        assertThat(Naming.concatSqlNames("", "bar")).isEqualTo("_bar");
        assertThat(Naming.concatSqlNames("_", "bar")).isEqualTo("_bar");
    }

    @Test
    public void camelToSnake_simple() {
        assertThat(Naming.camelToSnake("Foo")).isEqualTo("foo");
        assertThat(Naming.camelToSnake("FooBar")).isEqualTo("foo_bar");
        assertThat(Naming.camelToSnake("fooBar")).isEqualTo("foo_bar");
        assertThat(Naming.camelToSnake("Foo_Bar")).isEqualTo("foo__bar");

        assertThat(Naming.camelToSnake("_foo")).isEqualTo("_foo");
        assertThat(Naming.camelToSnake("Foo_")).isEqualTo("foo_");
        assertThat(Naming.camelToSnake("foo")).isEqualTo("foo");
        assertThat(Naming.camelToSnake("FOO")).isEqualTo("f_o_o");
    }
}
