package io.webby.util.func;

import com.google.common.base.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ReversibleFunctions {
    public static <A, B> @NotNull ReversibleFunction<A, B> fromNotNullFunctions(@NotNull Function<A, B> forward,
                                                                                @NotNull Function<B, A> backward) {
        LazyReversibleFunction<A, B> function = new LazyReversibleFunction<>() {
            @Override
            public @Nullable B apply(@Nullable A a) {
                return a != null ? forward.apply(a) : null;
            }
            @Override
            public @NotNull B applyNotNull(@NotNull A a) {
                return forward.apply(a);
            }
        };
        LazyReversibleFunction<B, A> reverse = new LazyReversibleFunction<>() {
            @Override
            public @Nullable A apply(@Nullable B b) {
                return b != null ? backward.apply(b) : null;
            }
            @Override
            public @NotNull A applyNotNull(@NotNull B b) {
                return backward.apply(b);
            }
        };
        LazyReversibleFunction.bind(function, reverse);
        return function;
    }

    public static <A, B> @NotNull ReversibleFunction<A, B> fromNullableFunctions(@NotNull Function<A, B> forward,
                                                                                 @NotNull Function<B, A> backward) {
        LazyReversibleFunction<A, B> function = new LazyReversibleFunction<>() {
            @Override
            public @Nullable B apply(@Nullable A a) {
                return forward.apply(a);
            }
            @Override
            public @NotNull B applyNotNull(@NotNull A a) {
                return requireNonNull(apply(a));
            }
        };
        LazyReversibleFunction<B, A> reverse = new LazyReversibleFunction<>() {
            @Override
            public @Nullable A apply(@Nullable B b) {
                return backward.apply(b);
            }
            @Override
            public @NotNull A applyNotNull(@NotNull B b) {
                return requireNonNull(apply(b));
            }
        };
        LazyReversibleFunction.bind(function, reverse);
        return function;
    }

    public static <A, B> @NotNull ReversibleFunction<A, B> fromNullableGuavaConverter(@NotNull Converter<A, B> converter) {
        return fromNullableFunctions(converter, converter.reverse());
    }

    public static <A, B> @NotNull ReversibleFunction<A, B> fromNotNullGuavaConverter(@NotNull Converter<A, B> converter) {
        return fromNotNullFunctions(converter, converter.reverse());
    }

    public static abstract class LazyReversibleFunction<U, V> implements ReversibleFunction<U, V> {
        private ReversibleFunction<V, U> lazyReverse = null;

        @Override
        public @NotNull ReversibleFunction<V, U> reverse() {
            assert lazyReverse != null : "ReversibleFunction not initialized: " + this;
            return lazyReverse;
        }

        private void initReverseOrDie(@NotNull ReversibleFunction<V, U> function) {
            assert lazyReverse == null : "ReversibleFunction already initialized with reverse function: " + lazyReverse;
            lazyReverse = function;
        }

        public static <A, B> void bind(@NotNull LazyReversibleFunction<A, B> forward,
                                       @NotNull LazyReversibleFunction<B, A> backward) {
            forward.initReverseOrDie(backward);
            backward.initReverseOrDie(forward);
        }
    }
}
