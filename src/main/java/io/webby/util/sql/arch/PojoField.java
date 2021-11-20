package io.webby.util.sql.arch;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.Immutable;
import io.webby.util.collect.Pair;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedAccessLazy;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Immutable
public abstract class PojoField {
    protected final PojoParent parent;
    protected final ModelField field;
    private final DelayedAccessLazy<String> lazyNameRef = AtomicLazy.emptyLazy();

    protected PojoField(@NotNull PojoParent parent, @NotNull ModelField field) {
        this.parent = parent;
        this.field = field;
    }

    public @NotNull PojoParent parent() {
        return parent;
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaGetter() {
        return field.getter();
    }

    public @NotNull String fullSqlName() {
        return lazyNameRef.lazyGet(() -> {
            Pair<Optional<String>, List<PojoField>> fullPath = fullPath();
            return Streams.concat(fullPath.first().stream(), fullPath.second().stream().map(PojoField::javaName))
                    .map(Naming::camelToSnake)
                    .collect(Collectors.joining("_"));
        });
    }

    private @NotNull Pair<Optional<String>, List<PojoField>> fullPath() {
        LinkedList<PojoField> parentFields = new LinkedList<>();
        PojoField current = this;
        while (true) {
            parentFields.add(current);
            PojoParent parent = current.parent;
            if (parent.isTerminal()) {
                Collections.reverse(parentFields);
                return Pair.of(parent.terminalFieldNameOrDie(), parentFields);
            } else {
                current = parent.pojoFieldOrDie();
            }
        }
    }

    public boolean isNativelySupported() {
        return false;
    }

    public abstract @NotNull AdapterInfo adapterInfo();

    public abstract @NotNull PojoField reattachedTo(@NotNull PojoParent parent);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoField that && Objects.equals(this.parent, that.parent) && Objects.equals(this.field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, field);
    }
}
