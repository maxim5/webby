package io.webby.orm.arch;

import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import io.webby.examples.model.NestedModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamingTest {
    @Test
    public void generatedSimpleJavaName_simple() {
        assertEquals("User", Naming.generatedSimpleJavaName(User.class));
        assertEquals("Session", Naming.generatedSimpleJavaName(Session.class));

        assertEquals("NestedModel", Naming.generatedSimpleJavaName(NestedModel.class));
        assertEquals("NestedModel_Level1", Naming.generatedSimpleJavaName(NestedModel.Level1.class));
        assertEquals("NestedModel_Simple", Naming.generatedSimpleJavaName(NestedModel.Simple.class));
    }

    @Test
    public void shortCanonicalJavaName_simple() {
        assertEquals("User", Naming.shortCanonicalJavaName(User.class));
        assertEquals("Session", Naming.shortCanonicalJavaName(Session.class));

        assertEquals("NestedModel", Naming.shortCanonicalJavaName(NestedModel.class));
        assertEquals("NestedModel.Level1", Naming.shortCanonicalJavaName(NestedModel.Level1.class));
        assertEquals("NestedModel.Simple", Naming.shortCanonicalJavaName(NestedModel.Simple.class));
    }

    @Test
    public void fieldSqlName_simple() {
        assertEquals("foo", Naming.fieldSqlName("Foo"));
        assertEquals("foo", Naming.fieldSqlName("_foo"));
        assertEquals("foo", Naming.fieldSqlName("Foo_"));
        assertEquals("foo", Naming.fieldSqlName("foo"));

        assertEquals("foo_bar", Naming.fieldSqlName("FooBar"));
        assertEquals("foo_bar", Naming.fieldSqlName("fooBar"));
        assertEquals("foo_bar", Naming.fieldSqlName("Foo_Bar"));
        assertEquals("foo_bar", Naming.fieldSqlName("Foo_Bar__"));

        assertEquals("foo_bar", Naming.fieldSqlName("foo_bar"));
        assertEquals("foo_bar", Naming.fieldSqlName("foo__bar"));

        assertEquals("foo_bar", Naming.fieldSqlName("Foo", "Bar"));
        assertEquals("foo_bar", Naming.fieldSqlName("foo", "bar"));
        assertEquals("foo_bar", Naming.fieldSqlName("_foo_", "_Bar_"));

        assertEquals("foo_", Naming.fieldSqlName("Foo", ""));
        assertEquals("foo_", Naming.fieldSqlName("Foo", "_"));
    }

    @Test
    public void modelSqlName_simple() {
        assertEquals("foo", Naming.modelSqlName("Foo"));
        assertEquals("foo", Naming.modelSqlName("_foo"));
        assertEquals("foo", Naming.modelSqlName("Foo_"));
        assertEquals("foo", Naming.modelSqlName("foo"));

        assertEquals("foo_bar", Naming.modelSqlName("FooBar"));
        assertEquals("foo_bar", Naming.modelSqlName("fooBar"));
        assertEquals("foo_bar", Naming.modelSqlName("Foo_Bar"));
        assertEquals("foo_bar", Naming.modelSqlName("Foo_Bar__"));

        assertEquals("foo_bar", Naming.modelSqlName("foo_bar"));
        assertEquals("foo_bar", Naming.modelSqlName("foo__bar"));
    }

    @Test
    public void camelToSnake_simple() {
        assertEquals("foo", Naming.camelToSnake("Foo"));
        assertEquals("foo_bar", Naming.camelToSnake("FooBar"));
        assertEquals("foo_bar", Naming.camelToSnake("fooBar"));
        assertEquals("foo__bar", Naming.camelToSnake("Foo_Bar"));

        assertEquals("_foo", Naming.camelToSnake("_foo"));
        assertEquals("foo_", Naming.camelToSnake("Foo_"));
        assertEquals("foo", Naming.camelToSnake("foo"));
        assertEquals("f_o_o", Naming.camelToSnake("FOO"));
    }
}