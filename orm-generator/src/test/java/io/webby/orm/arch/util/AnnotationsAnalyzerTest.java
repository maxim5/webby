package io.webby.orm.arch.util;

import com.google.common.truth.BooleanSubject;
import com.google.common.truth.ClassSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StringSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.annotate.Model;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.api.annotate.Sql.*;
import io.webby.orm.arch.InvalidSqlModelException;
import io.webby.orm.arch.factory.ModelInput;
import io.spbx.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnnotationsAnalyzerTest {
    @Test
    public void getSqlName_annotations() {
        record FooPojo(@Sql(name = "foo") int foo, @Sql @Name("bar") int bar, int baz) {}

        fromJavaClass(FooPojo.class).ofField("foo").hasSqlName().isEqualTo("foo");
        fromJavaClass(FooPojo.class).ofField("bar").hasSqlName().isEqualTo("bar");
        fromJavaClass(FooPojo.class).ofField("baz").hasSqlName().isNull();
    }

    @Test
    public void getSqlName_invalid() {
        record FooPojo(@Sql(name = "foo") @Name("bar") int invalid) {}

        fromJavaClass(FooPojo.class).ofField("invalid").sqlNameThrows();
    }

    @Test
    public void isPrimaryKeyField_default_ids() {
        record FooPojo(int id, int fooPojoId, int pojoId, int foo_pojo_id, int foo, int bar) {}

        fromJavaClass(FooPojo.class).ofField("id").isPrimaryKeyField().isTrue();
        fromJavaClass(FooPojo.class).ofField("fooPojoId").isPrimaryKeyField().isTrue();
        fromJavaClass(FooPojo.class).ofField("pojoId").isPrimaryKeyField().isFalse();
        fromJavaClass(FooPojo.class).ofField("foo_pojo_id").isPrimaryKeyField().isFalse();
        fromJavaClass(FooPojo.class).ofField("foo").isPrimaryKeyField().isFalse();
        fromJavaClass(FooPojo.class).ofField("bar").isPrimaryKeyField().isFalse();
    }

    @Test
    public void isPrimaryKeyField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(primary = true) int foo, @PK int bar, int pojoId) {}

        fromJavaClass(FooPojo.class).ofField("foo").isPrimaryKeyField().isTrue();
        fromJavaClass(FooPojo.class).ofField("bar").isPrimaryKeyField().isTrue();
        fromJavaClass(FooPojo.class).ofField("pojoId").isPrimaryKeyField().isTrue();
    }

    @Test
    public void isUniqueField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(unique = true) int foo, @Unique int bar, int baz) {}

        fromJavaClass(FooPojo.class).ofField("foo").isUniqueField().isTrue();
        fromJavaClass(FooPojo.class).ofField("bar").isUniqueField().isTrue();
        fromJavaClass(FooPojo.class).ofField("baz").isUniqueField().isFalse();
    }

    @Test
    public void isNullableField_annotations() {
        @Model(javaName = "Pojo")
        record FooPojo(@Sql(nullable = true) int foo, @Null int bar, @javax.annotation.Nullable Long baz) {}

        fromJavaClass(FooPojo.class).ofField("foo").isNullableField().isTrue();
        fromJavaClass(FooPojo.class).ofField("bar").isNullableField().isTrue();
        fromJavaClass(FooPojo.class).ofField("baz").isNullableField().isTrue();
    }

    @Test
    public void getDefaults_annotations() {
        record Foo(int a, @Default("x") int b, @Sql @Default("y") int c, @Sql(defaults = "z") int d) {}

        fromJavaClass(Foo.class).ofField("a").defaults().isNull();
        fromJavaClass(Foo.class).ofField("b").defaults().asList().containsExactly("x");
        fromJavaClass(Foo.class).ofField("c").defaults().asList().containsExactly("y");
        fromJavaClass(Foo.class).ofField("d").defaults().asList().containsExactly("z");
    }

    @Test
    public void getDefaults_invalid() {
        record Foo(@Sql(defaults = "foo") @Default("bar") int invalid) {}

        fromJavaClass(Foo.class).ofField("invalid").defaultsThrows();
    }

    @Test
    public void getViaClass_annotations() {
        record Foo(int a, @Via(Object.class) int b, @Sql @Via int c, @Sql(via = Object.class) int d) {}

        fromJavaClass(Foo.class).ofField("a").viaClass().isNull();
        fromJavaClass(Foo.class).ofField("b").viaClass().isEqualTo(Object.class);
        fromJavaClass(Foo.class).ofField("c").viaClass().isNull();
        fromJavaClass(Foo.class).ofField("d").viaClass().isEqualTo(Object.class);
    }

    @Test
    public void getViaClass_invalid() {
        record Foo(@Sql(via = Object.class) @Via(Object.class) int invalid) {}

        fromJavaClass(Foo.class).ofField("invalid").viaClassThrows();
    }

    private static @NotNull JavaClassAnalyzerSubject fromJavaClass(@NotNull Class<?> klass) {
        return new JavaClassAnalyzerSubject(klass);
    }

    private record JavaClassAnalyzerSubject(@NotNull Class<?> klass) {
        public @NotNull AnnotatedElementSubject ofField(@NotNull String name) {
            Field field = requireNonNull(EasyMembers.findField(klass, name));
            return new AnnotatedElementSubject(field, klass);
        }
    }

    private record AnnotatedElementSubject(@NotNull AnnotatedElement element, @NotNull Class<?> klass) {
        public @NotNull StringSubject hasSqlName() {
            Optional<String> sqlName = AnnotationsAnalyzer.getSqlName(element);
            return assertThat(sqlName.orElse(null));
        }

        @CanIgnoreReturnValue
        public @NotNull InvalidSqlModelException sqlNameThrows() {
            return assertThrows(InvalidSqlModelException.class, () -> AnnotationsAnalyzer.getSqlName(element));
        }

        public @NotNull BooleanSubject isPrimaryKeyField() {
            boolean isPrimaryKey = AnnotationsAnalyzer.isPrimaryKeyField((Field) element, ModelInput.of(klass));
            return assertThat(isPrimaryKey);
        }

        public @NotNull BooleanSubject isUniqueField() {
            boolean isUnique = AnnotationsAnalyzer.isUniqueField(element);
            return assertThat(isUnique);
        }

        public @NotNull BooleanSubject isNullableField() {
            boolean isNullable = AnnotationsAnalyzer.isNullableField((Field) element);
            return assertThat(isNullable);
        }

        public @NotNull ObjectArraySubject<@Nullable String> defaults() {
            @Nullable String @Nullable [] defaults = AnnotationsAnalyzer.getDefaults(element);
            return assertThat(defaults);
        }

        @CanIgnoreReturnValue
        public @NotNull InvalidSqlModelException defaultsThrows() {
            return assertThrows(InvalidSqlModelException.class, () -> AnnotationsAnalyzer.getDefaults(element));
        }

        public @NotNull ClassSubject viaClass() {
            Class<?> viaClass = AnnotationsAnalyzer.getViaClass(element);
            return assertThat(viaClass);
        }

        @CanIgnoreReturnValue
        public @NotNull InvalidSqlModelException viaClassThrows() {
            return assertThrows(InvalidSqlModelException.class, () -> AnnotationsAnalyzer.getViaClass(element));
        }
    }
}
