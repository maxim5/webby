package io.webby.util.sql.arch;

import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JavaClassAnalyzerTest {
    @Test
    public void getAllFieldsOrdered_class_implements_interface() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrdered(FooClassInterface.class), "i");
    }

    static class FooClassInterface implements Serializable {
        private int i;
        public FooClassInterface(int i) {
            this.i = i;
        }
    }

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
    public void findGetterMethod_pojo_boolean() {
        class FooPojo {
            private boolean enabled;
            private boolean disabled;
            private boolean off;

            public boolean isEnabled() { return enabled; }
            public boolean disabled() { return disabled; }
            public boolean getOff() { return off; }
        }

        assertMethod(findGetterMethod(FooPojo.class, "enabled"), "isEnabled");
        assertMethod(findGetterMethod(FooPojo.class, "disabled"), "disabled");
        assertMethod(findGetterMethod(FooPojo.class, "off"), "getOff");
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

    private static void assertFields(@NotNull List<Field> fields, @NotNull String ... names) {
        assertThat(fields.stream().map(Field::getName).toList()).containsExactlyElementsIn(names);
    }

    private static @Nullable Method findGetterMethod(@NotNull Class<?> klass, @NotNull String name) {
        Field field = EasyMembers.findField(klass, name);
        assertNotNull(field);
        return JavaClassAnalyzer.findGetterMethod(field);
    }

    private static void assertMethod(@Nullable Method method, @NotNull String name) {
        assertNotNull(method);
        assertEquals(name, method.getName());
    }
}
