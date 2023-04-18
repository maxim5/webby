package io.webby.orm.arch.factory;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.api.annotate.Sql.Null;
import io.webby.orm.api.annotate.Sql.PK;
import io.webby.orm.api.annotate.Sql.Unique;
import io.webby.orm.api.annotate.Sql.Via;
import io.webby.orm.arch.InvalidSqlModelException;
import io.webby.orm.codegen.ModelInput;
import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void isPrimaryKeyField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(primary = true) int foo, @PK int bar, int pojoId) {}

        assertJavaClass(FooPojo.class)
            .hasPrimaryKeyField("foo")
            .hasPrimaryKeyField("bar")
            .hasPrimaryKeyField("pojoId");
    }

    @Test
    public void isUniqueField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(unique = true) int foo, @Unique int bar, int baz) {}

        assertJavaClass(FooPojo.class)
            .hasUniqueField("foo")
            .hasUniqueField("bar")
            .hasNotUniqueField("baz");
    }

    @Test
    public void isNullableField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(nullable = true) int foo, @Null int bar, @javax.annotation.Nullable Long baz) {}

        assertJavaClass(FooPojo.class)
            .hasNullableField("foo")
            .hasNullableField("bar")
            .hasNullableField("baz");
    }

    @Test
    public void getViaClass_annotations() {
        record Foo(int a, @Via(Object.class) int b, @Via int c, @Sql(via = Object.class) int d) {}

        assertJavaClass(Foo.class).ofField("a").hasNoViaClass();
        assertJavaClass(Foo.class).ofField("b").hasViaClass(Object.class);
        assertJavaClass(Foo.class).ofField("c").hasNoViaClass();
        assertJavaClass(Foo.class).ofField("d").hasViaClass(Object.class);
    }

    @Test
    public void getViaClass_invalid() {
        record Foo(@Sql(via = Object.class) @Via(Object.class) int invalid) {}

        assertJavaClass(Foo.class).ofField("invalid").viaClassThrows();
    }

    private static void assertFields(@NotNull List<Field> fields, @NotNull String ... names) {
        assertThat(fields.stream().map(Field::getName).toList()).containsExactlyElementsIn(names);
    }

    private static @NotNull JavaClassAnalyzerSubject assertJavaClass(@NotNull Class<?> klass) {
        return new JavaClassAnalyzerSubject(klass);
    }

    private static @NotNull AnnotatedElementSubject assertAnnotation(@NotNull AnnotatedElement element) {
        return new AnnotatedElementSubject(element);
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

        public @NotNull AnnotatedElementSubject ofField(@NotNull String name) {
            Field field = getFieldByName(name);
            return assertAnnotation(field);
        }

        public @NotNull JavaClassAnalyzerSubject hasPrimaryKeyField(@NotNull String name) {
            boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(getFieldByName(name), ModelInput.of(klass));
            assertThat(isPrimaryKey).isTrue();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasNotPrimaryKeyField(@NotNull String name) {
            boolean isPrimaryKey = JavaClassAnalyzer.isPrimaryKeyField(getFieldByName(name), ModelInput.of(klass));
            assertThat(isPrimaryKey).isFalse();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasUniqueField(@NotNull String name) {
            boolean isUnique = JavaClassAnalyzer.isUniqueField(getFieldByName(name));
            assertThat(isUnique).isTrue();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasNotUniqueField(@NotNull String name) {
            boolean isUnique = JavaClassAnalyzer.isUniqueField(getFieldByName(name));
            assertThat(isUnique).isFalse();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasNullableField(@NotNull String name) {
            boolean isNullable = JavaClassAnalyzer.isNullableField(getFieldByName(name));
            assertThat(isNullable).isTrue();
            return this;
        }

        public @NotNull JavaClassAnalyzerSubject hasNotNullableField(@NotNull String name) {
            boolean isNullable = JavaClassAnalyzer.isNullableField(getFieldByName(name));
            assertThat(isNullable).isFalse();
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

    @CanIgnoreReturnValue
    private record AnnotatedElementSubject(@NotNull AnnotatedElement element) {
        public @NotNull AnnotatedElementSubject hasViaClass(@NotNull Class<?> klass) {
            Class<?> viaClass = JavaClassAnalyzer.getViaClass(element);
            assertThat(viaClass).isEqualTo(klass);
            return this;
        }

        public @NotNull AnnotatedElementSubject hasNoViaClass() {
            Class<?> viaClass = JavaClassAnalyzer.getViaClass(element);
            assertThat(viaClass).isNull();
            return this;
        }

        public @NotNull AnnotatedElementSubject viaClassThrows() {
            assertThrows(InvalidSqlModelException.class, () -> JavaClassAnalyzer.getViaClass(element));
            return this;
        }
    }
}
