package io.webby.orm.arch.model;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.Immutable;
import io.webby.util.base.Pair;
import io.webby.util.lazy.AtomicCacheCompute;
import io.webby.util.lazy.CacheCompute;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Immutable
public abstract class PojoField {
    protected final PojoParent parent;
    protected final ModelField field;
    protected final TypeSupport typeSupport;
    private final CacheCompute<String> lazyNameRef = AtomicCacheCompute.createEmpty();

    protected PojoField(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull TypeSupport typeSupport) {
        this.parent = parent;
        this.field = field;
        this.typeSupport = typeSupport;
    }

    public @NotNull PojoParent parent() {
        return parent;
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaAccessor() {
        return field.accessor();
    }

    public @NotNull String fullSqlName() {
        return lazyNameRef.getOrCompute(() -> {
            Pair<Optional<String>, List<String>> fullPath = fullSqlPath();
            return Streams.concat(fullPath.first().stream(), fullPath.second().stream()).collect(Collectors.joining("_"));
        });
    }

    private @NotNull Pair<Optional<String>, List<String>> fullSqlPath() {
        LinkedList<PojoField> parentFields = new LinkedList<>();
        PojoField current = this;
        while (true) {
            parentFields.add(current);
            PojoParent parent = current.parent;
            if (parent.isTerminal()) {
                Collections.reverse(parentFields);
                return Pair.of(parent.terminalSqlNameOrDie(),
                               parentFields.stream().map(field -> field.field.sqlName()).toList());
            } else {
                current = parent.pojoFieldOrDie();
            }
        }
    }

    public @NotNull TypeSupport typeSupport() {
        return typeSupport;
    }

    public @NotNull MapperApi mapperApiOrDie() {
        throw new IllegalStateException("Internal error. Field does not have a mapper: " + this);
    }

    public @NotNull AdapterApi adapterApiOrDie() {
        throw new IllegalStateException("Internal error. Field does not have an adapter: " + this);
    }

    public abstract @NotNull PojoField reattachedTo(@NotNull PojoParent parent);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoField that && Objects.equals(this.parent, that.parent) &&
            Objects.equals(this.field, that.field) && Objects.equals(this.typeSupport, that.typeSupport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, field, typeSupport);
    }

    public enum TypeSupport {
        NATIVE,
        MAPPER_API,
        ADAPTER_API,
    }
}
