package io.spbx.util.func;

import com.google.common.base.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Reversibles {
    public static <A, B> @NotNull Reversible<A, B> fromNotNullFunctions(@NotNull Function<A, B> forward,
                                                                        @NotNull Function<B, A> backward) {
        LazyReversible<A, B> function = new LazyReversible<>() {
            @Override
            public @NotNull B forward(@NotNull A a) {
                return forward.apply(a);
            }
        };
        LazyReversible<B, A> reverse = new LazyReversible<>() {
            @Override
            public @NotNull A forward(@NotNull B b) {
                return backward.apply(b);
            }
        };
        LazyReversible.bind(function, reverse);
        return function;
    }

    public static <A, B> @NotNull Reversible<A, B> fromNullableFunctions(@NotNull Function<A, B> forward,
                                                                         @NotNull Function<B, A> backward) {
        LazyReversible<A, B> function = new LazyReversible<>() {
            @Override
            public @NotNull B forward(@NotNull A a) {
                return requireNonNull(apply(a));
            }
            @Override
            public @Nullable B forwardNullable(@Nullable A a) {
                return forward.apply(a);
            }
        };
        LazyReversible<B, A> reverse = new LazyReversible<>() {
            @Override
            public @NotNull A forward(@NotNull B b) {
                return requireNonNull(apply(b));
            }
            @Override
            public @Nullable A forwardNullable(@Nullable B b) {
                return backward.apply(b);
            }
        };
        LazyReversible.bind(function, reverse);
        return function;
    }

    public static <A, B> @NotNull Reversible<A, B> fromNullableGuavaConverter(@NotNull Converter<A, B> converter) {
        return fromNullableFunctions(converter, converter.reverse());
    }

    public static <A, B> @NotNull Reversible<A, B> fromNotNullGuavaConverter(@NotNull Converter<A, B> converter) {
        return fromNotNullFunctions(converter, converter.reverse());
    }

    public static abstract class LazyReversible<U, V> implements Reversible<U, V> {
        private Reversible<V, U> lazyReverse = null;

        @Override
        public @NotNull U backward(@NotNull V v) {
            return reverse().forward(v);
        }

        @Override
        public @NotNull Reversible<V, U> reverse() {
            assert lazyReverse != null : "ReversibleFunction not initialized: " + this;
            return lazyReverse;
        }

        private void initReverseOrDie(@NotNull Reversible<V, U> function) {
            assert lazyReverse == null : "ReversibleFunction already initialized with reverse function: " + lazyReverse;
            lazyReverse = function;
        }

        public static <A, B> void bind(@NotNull Reversibles.LazyReversible<A, B> forward,
                                       @NotNull Reversibles.LazyReversible<B, A> backward) {
            forward.initReverseOrDie(backward);
            backward.initReverseOrDie(forward);
        }
    }
}
