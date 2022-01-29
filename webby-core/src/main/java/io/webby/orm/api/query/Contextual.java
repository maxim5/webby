package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Contextual<Q extends HasArgs, C> implements Representable {
    private final Q query;

    public Contextual(@NotNull Q query) {
        this.query = query;
    }

    public @NotNull Q query() {
        return query;
    }

    @Override
    public @NotNull String repr() {
        return query.repr();
    }

    public abstract @NotNull Args resolveQueryArgs(@NotNull C context);

    public static <Q extends HasArgs, C> @NotNull Contextual<Q, C> constant(@NotNull Q query) {
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                return query.args();
            }
        };
    }

    public static <Q extends HasArgs, C>
            @NotNull Contextual<Q, C> resolvingByName(@NotNull Q query, @NotNull Function<C, Map<String, ?>> resolver) {
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                Map<String, ?> map = resolver.apply(context);
                return query.args().resolveArgsByName(map);
            }
        };
    }

    public static <Q extends HasArgs, C>
            @NotNull Contextual<Q, C> resolvingByOrderedList(@NotNull Q query, @NotNull Function<C, List<?>> resolver) {
        return new Contextual<>(query) {
            @Override
            public @NotNull Args resolveQueryArgs(@NotNull C context) {
                List<?> args = resolver.apply(context);
                return query.args().resolveArgsByOrderedList(args);
            }
        };
    }

    // FIX[norm]: resolvingByInts, resolvingByLongs
}
