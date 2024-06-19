package io.spbx.util.reflect;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.inject.internal.MoreTypes;
import io.spbx.util.func.Reversible;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

public class EasyGenericsTest {
    @Test
    public void getGenericTypeArgumentsOfField_optional() {
        @SuppressWarnings("rawtypes")
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
    public void getGenericTypeArgumentsOfInterface_anonymous_implementation() {
        Reversible<String, Integer> reversible = new Reversible<>() {
            @Override public @NotNull Integer forward(@NotNull String s) { return 0; }
            @Override public @NotNull String backward(@NotNull Integer integer) { return ""; }
        };

        assertJavaClass(reversible.getClass())
            .findsGenericTypeArgumentsForInterface(Reversible.class, String.class, Integer.class);
    }

    @CheckReturnValue
    private static @NotNull EasyGenericsSubject assertJavaClass(@NotNull Class<?> klass) {
        return new EasyGenericsSubject(klass);
    }

    @CanIgnoreReturnValue
    private record EasyGenericsSubject(@NotNull Class<?> klass) {
        public @NotNull EasyGenericsSubject findsGenericTypeArgumentsForField(@NotNull String name,
                                                                              @NotNull Type... types) {
            Field field = requireNonNull(EasyMembers.findField(klass, name));
            Type[] typeArguments = EasyGenerics.getGenericTypeArgumentsOfField(field);
            assertThat(typeArguments).asList().containsExactly((Object[]) types).inOrder();
            return this;
        }

        public @NotNull EasyGenericsSubject findsGenericTypeArgumentsForInterface(@NotNull Class<?> interfaceType,
                                                                                  @NotNull Type ... types) {
            Type[] typeArguments = EasyGenerics.getGenericTypeArgumentsOfInterface(klass, interfaceType);
            assertThat(typeArguments).asList().containsExactly((Object[]) types).inOrder();
            return this;
        }
    }

    private static @NotNull Type getWildcardType() {
        return new MoreTypes.WildcardTypeImpl(new Type[] { Object.class }, new Type[0]);
    }
}
