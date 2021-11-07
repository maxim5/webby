package io.webby.util.sql.schema;

import com.google.common.collect.Streams;
import io.webby.util.collect.OneOf;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record PojoField(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull OneOf<JdbcType, PojoSchema> ref) {
    public static @NotNull PojoField ofNative(@NotNull ModelField field, @NotNull JdbcType jdbcType) {
        return new PojoField(PojoParent.DETACHED, field, OneOf.ofFirst(jdbcType));
    }

    public static @NotNull PojoField ofEnum(@NotNull Class<?> type) {
        assert type.isEnum() : "Type is not an enum: %s".formatted(type);
        ModelField field = new ModelField("ord", "ordinal", type, type);
        return ofNative(field, JdbcType.Int);
    }

    public static @NotNull PojoField ofSubPojo(@NotNull ModelField field, @NotNull PojoSchema pojo) {
        return newReattachedSubPojo(PojoParent.DETACHED, field, pojo);
    }

    public @NotNull String javaName() {
        return field.name();
    }

    public @NotNull String javaGetter() {
        return field.getter();
    }

    public @NotNull String fullSqlName() {
        Pair<Optional<String>, List<PojoField>> fullPath = fullPath();
        return Streams.concat(fullPath.first().stream(), fullPath.second().stream().map(PojoField::javaName))
                .map(Naming::camelToSnake)
                .collect(Collectors.joining("_"));
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
        return ref.hasFirst();
    }

    public @NotNull JdbcType jdbcTypeOrDie() {
        return requireNonNull(ref.first());
    }

    public boolean hasPojo() {
        return ref.hasSecond();
    }

    public @NotNull PojoSchema pojoOrDie() {
        return requireNonNull(ref.second());
    }

    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        if (hasPojo()) {
            return newReattachedSubPojo(parent, field, pojoOrDie());
        }
        return new PojoField(parent, field, ref);
    }

    private static @NotNull PojoField newReattachedSubPojo(@NotNull PojoParent parent,
                                                           @NotNull ModelField field,
                                                           @NotNull PojoSchema pojo) {
        PojoParent thisParent = PojoParent.ofUninitializedField();
        PojoSchema copy = pojo.reattachedTo(thisParent);
        PojoField thisField = new PojoField(parent, field, OneOf.ofSecond(copy));
        thisParent.initializeOrDie(thisField);
        return thisField;
    }
}
