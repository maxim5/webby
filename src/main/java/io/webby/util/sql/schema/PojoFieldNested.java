package io.webby.util.sql.schema;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Immutable
public class PojoFieldNested extends PojoField {
    private final PojoSchema pojo;

    protected PojoFieldNested(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull PojoSchema pojo) {
        super(parent, field);
        this.pojo = pojo;
    }

    public static @NotNull PojoField ofNestedPojo(@NotNull ModelField field, @NotNull PojoSchema pojo) {
        return newReattachedSubPojo(PojoParent.DETACHED, field, pojo);
    }

    public @NotNull PojoSchema pojo() {
        return pojo;
    }

    @Override
    public @NotNull AdapterInfo adapterInfo() {
        return AdapterInfo.ofSignature(pojo());
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return newReattachedSubPojo(parent, field, pojo());
    }

    private static @NotNull PojoField newReattachedSubPojo(@NotNull PojoParent parent,
                                                           @NotNull ModelField field,
                                                           @NotNull PojoSchema pojo) {
        PojoParent thisParent = PojoParent.ofUninitializedField();
        PojoSchema copy = pojo.reattachedTo(thisParent);
        PojoField thisField = new PojoFieldNested(parent, field, copy);
        thisParent.initializeOrDie(thisField);
        return thisField;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldNested that && super.equals(obj) && Objects.equals(this.pojo, that.pojo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pojo);
    }

    @Override
    public String toString() {
        return "PojoFieldNested[parent=%s, field=%s, pojo=%s]".formatted(parent, field, pojo);
    }
}