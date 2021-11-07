package io.webby.util.sql.schema;

import io.webby.util.reflect.EasyClasspath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class ModelSchemaFactoryTest {
    @Test
    public void findGetterMethod_record_plain() {
        record FooRecord(int i, long l, String s) {}

        assertMethod(findGetterMethod(FooRecord.class, "i"), "i");
        assertMethod(findGetterMethod(FooRecord.class, "l"), "l");
        assertMethod(findGetterMethod(FooRecord.class, "s"), "s");
    }

    @Test
    public void findGetterMethod_record_with_getters() {
        record FooRecord(int i, long l, String s) {
            public int getI() { return i; }
            public String getS() { return s; }
        }

        assertMethod(findGetterMethod(FooRecord.class, "i"), "i");
        assertMethod(findGetterMethod(FooRecord.class, "l"), "l");
        assertMethod(findGetterMethod(FooRecord.class, "s"), "s");
    }

    @Test
    public void findGetterMethod_pojo_getters() {
        class FooPojo {
            private int i;
            private long l;
            private String s;

            public int getI() { return i; }
            public long getL() { return l; }
            public String getS() { return s; }
        }

        assertMethod(findGetterMethod(FooPojo.class, "i"), "getI");
        assertMethod(findGetterMethod(FooPojo.class, "l"), "getL");
        assertMethod(findGetterMethod(FooPojo.class, "s"), "getS");
    }

    @Test
    public void findGetterMethod_pojo_accessors() {
        class FooPojo {
            private int i;
            private long l;
            private String s;

            public int i() { return i; }
            public long l() { return l; }
            public String s() { return s; }
        }

        assertMethod(findGetterMethod(FooPojo.class, "i"), "i");
        assertMethod(findGetterMethod(FooPojo.class, "l"), "l");
        assertMethod(findGetterMethod(FooPojo.class, "s"), "s");
    }

    @Test
    public void findGetterMethod_pojo_do_not_match_object_methods() {
        class FooPojo {
            private int i;
            private String s;
        }

        assertNull(findGetterMethod(FooPojo.class, "i"));
        assertNull(findGetterMethod(FooPojo.class, "s"));
    }

    @Test
    public void findGetterMethod_pojo_do_not_match_unrelated_methods() {
        class FooPojo {
            private int foo;
            private long bar;
            private String baz;

            public int value() { return foo; }
            public long barCode() { return bar; }
            public String bazValue() { return baz; }
        }

        assertNull(findGetterMethod(FooPojo.class, "foo"));
        assertNull(findGetterMethod(FooPojo.class, "bar"));
        assertNull(findGetterMethod(FooPojo.class, "baz"));
    }

    private static @Nullable Method findGetterMethod(@NotNull Class<?> klass, @NotNull String name) {
        Field field = EasyClasspath.findField(klass, EasyClasspath.Scope.DECLARED, name);
        assertNotNull(field);
        return ModelSchemaFactory.findGetterMethod(field);
    }

    private static void assertMethod(@Nullable Method method, @NotNull String name) {
        assertNotNull(method);
        assertEquals(name, method.getName());
    }
}
