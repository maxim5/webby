package io.webby.orm.arch.factory;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.internal.MoreTypes;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.codegen.ModelInput;
import io.webby.util.func.Reversible;
import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JavaClassAnalyzerTest {
    @Test
    public void getAllFieldsOrdered_class() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrdered(Point.class), "x", "y");
    }

    @Test
    public void getAllFieldsOrdered_class_implements_interface() {
        assertFields(JavaClassAnalyzer.getAllFieldsOrdered(FooClassInterface.class), "i");
    }

    @SuppressWarnings("FieldMayBeFinal")
    static class FooClassInterface implements Serializable {
        private int i;
        public FooClassInterface(int i) {
            this.i = i;
        }
    }

    @Test
    public void findGetterMethod_record_plain() {
        record FooRecord(int i, long l, String s) {}

        assertJavaClass(FooRecord.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethod_record_with_getters() {
        record FooRecord(int i, long l, String s) {
            public int getI() { return i; }
            public String getS() { return s; }
        }

        assertJavaClass(FooRecord.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
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

        assertJavaClass(FooPojo.class)
            .findsMethod("i", "getI")
            .findsMethod("l", "getL")
            .findsMethod("s", "getS");
    }

    @Test
    public void findGetterMethod_pojo_standard_point() {
        assertJavaClass(Point.class)
            .doesNotFindMethod("x")
            .doesNotFindMethod("y");
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

        assertJavaClass(FooPojo.class)
            .findsMethod("enabled", "isEnabled")
            .findsMethod("disabled", "disabled")
            .findsMethod("off", "getOff");
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

        assertJavaClass(FooPojo.class)
            .findsMethod("i", "i")
            .findsMethod("l", "l")
            .findsMethod("s", "s");
    }

    @Test
    public void findGetterMethod_pojo_do_not_match_object_methods() {
        class FooPojo {
            private int i;
            private String s;
            // hashCode() and toString()
        }

        assertJavaClass(FooPojo.class)
            .doesNotFindMethod("i")
            .doesNotFindMethod("s");
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

        assertJavaClass(FooPojo.class)
            .doesNotFindMethod("foo")
            .doesNotFindMethod("bar")
            .doesNotFindMethod("baz");
    }

    @Test
    public void getGenericTypeArgumentsOfField_optional() {
        record Foo(Optional<Integer> ints, Optional<String> str, Optional<Object> obj, Optional<?> wild, Optional opt) {}

        assertJavaClass(Foo.class)
            .findsGenericTypeArgumentsForField("ints", Integer.class)
            .findsGenericTypeArgumentsForField("str", String.class)
            .findsGenericTypeArgumentsForField("obj", Object.class)
            .findsGenericTypeArgumentsForField("wild", getWildcardType())
            .findsGenericTypeArgumentsForField("opt");
    }

    @Test
    public void getGenericTypeArgumentsOfInterface_implements_interface_directly() {
        class Foo implements Reversible<String, Integer> {
            @Override public @NotNull Integer forward(@NotNull String s) { return 0; }
            @Override public @NotNull String backward(@NotNull Integer integer) { return ""; }
        }

        assertJavaClass(Foo.class)
            .findsGenericTypeArgumentsForInterface(Reversible.class, String.class, Integer.class);
    }

    @Test
    public void getGenericTypeArgumentsOfInterface_implements_interface_chain_with_args() {
        interface Foo<T> extends Reversible<String, T> {}
        class Bar implements Foo<Integer> {
            @Override public @NotNull Integer forward(@NotNull String s) { return 0; }
            @Override public @NotNull String backward(@NotNull Integer integer) { return ""; }
        }

        assertJavaClass(Bar.class)
            .findsGenericTypeArgumentsForInterface(Reversible.class, String.class, Integer.class);
    }

    @Test
    public void isPrimaryKeyField_default_ids() {
        record FooPojo(int id, int fooPojoId, int pojoId, int foo_pojo_id, int foo, int bar) {}

        assertJavaClass(FooPojo.class)
            .hasPrimaryKeyField("id")
            .hasPrimaryKeyField("fooPojoId")
            .hasNotPrimaryKeyField("pojoId")
            .hasNotPrimaryKeyField("foo_pojo_id")
            .hasNotPrimaryKeyField("foo")
            .hasNotPrimaryKeyField("bar");
    }

    @Test
    public void isPrimaryKeyField_annotation() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(primary = true) int foo, int pojoId) {}

        assertJavaClass(FooPojo.class)
            .hasPrimaryKeyField("foo")
            .hasPrimaryKeyField("pojoId");
    }

    private static void assertFields(@NotNull List<Field> fields, @NotNull String ... names) {
        assertThat(fields.stream().map(Field::getName).toList()).containsExactlyElementsIn(names);
    }

    private static @NotNull JavaClassAnalyzerSubject assertJavaClass(@NotNull Class<?> klass) {
        return new JavaClassAnalyzerSubject(klass);
    }

    @CanIgnoreReturnValue
    private record JavaClassAnalyzerSubject(@NotNull Class<?> klass) {
        public @NotNull JavaClassAnalyzerSubject findsMethod(@NotNull String name, @NotNull String expected) {
            Method getterMethod = findGetterMethod(name);
            assertNotNull(getterMethod);
            assertThat(getterMethod.getName()).isEqualTo(expected);
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject doesNotFindMethod(@NotNull String name) {
            Method getterMethod = findGetterMethod(name);
            assertThat(getterMethod).isNull();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject findsGenericTypeArgumentsForField(@NotNull String name,
                                                                                   @NotNull Type ... types) {
            Field field = getFieldByName(name);
            Type[] typeArguments = JavaClassAnalyzer.getGenericTypeArgumentsOfField(field);
            assertThat(typeArguments).asList().containsExactly((Object[]) types).inOrder();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject findsGenericTypeArgumentsForInterface(@NotNull Class<?> interfaceType,
                                                                                       @NotNull Type ... types) {
            Type[] typeArguments = JavaClassAnalyzer.getGenericTypeArgumentsOfInterface(klass, interfaceType);
            assertThat(typeArguments).asList().containsExactly((Object[]) types).inOrder();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasPrimaryKeyField(@NotNull String name) {
            Field field = getFieldByName(name);
            boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(field, ModelInput.of(klass));
            assertThat(isPrimaryKey).isTrue();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasNotPrimaryKeyField(@NotNull String name) {
            Field field = getFieldByName(name);
            boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(field, ModelInput.of(klass));
            assertThat(isPrimaryKey).isFalse();
            return this;
        }

        @Nullable
        private Method findGetterMethod(@NotNull String name) {
            return JavaClassAnalyzer.findGetterMethod(getFieldByName(name));
        }

        @NotNull
        private Field getFieldByName(@NotNull String name) {
            Field field = EasyMembers.findField(klass, name);
            assertNotNull(field);
            return field;
        }
    }

    private static @NotNull Type getWildcardType() {
        return new MoreTypes.WildcardTypeImpl(new Type[]{Object.class}, new Type[0]);
    }
}
