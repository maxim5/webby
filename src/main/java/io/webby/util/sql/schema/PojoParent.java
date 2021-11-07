package io.webby.util.sql.schema;

import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedInitLazy;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class PojoParent {
    public static final PojoParent DETACHED = new PojoParent(OneOf.ofFirst(Optional.empty()));

    private final OneOf<Optional<String>, DelayedInitLazy<PojoField>> parent;

    private PojoParent(@NotNull OneOf<Optional<String>, DelayedInitLazy<PojoField>> parent) {
        this.parent = parent;
    }

    public static @NotNull PojoParent ofTerminal(@NotNull String fieldName) {
        return new PojoParent(OneOf.ofFirst(Optional.of(fieldName)));
    }

    /*package*/ static @NotNull PojoParent ofUninitializedField() {
        return new PojoParent(OneOf.ofSecond(AtomicLazy.ofUninitialized()));
    }

    /*package*/ void initializeOrDie(@NotNull PojoField field) {
        assert parent.hasSecond() : "Parent expected to hold pojo: %s".formatted(this);
        requireNonNull(parent.second()).initializeOrDie(field);
    }

    public boolean isTerminal() {
        return parent.hasFirst();
    }

    public @NotNull Optional<String> terminalFieldNameOrDie() {
        return requireNonNull(parent.first());
    }

    public @NotNull PojoField pojoFieldOrDie() {
        return requireNonNull(parent.second()).getOrDie();
    }

    @Override
    public String toString() {
        return "PojoParent%s".formatted(parent);
    }
}
