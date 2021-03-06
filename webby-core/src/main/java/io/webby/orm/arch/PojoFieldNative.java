package io.webby.orm.arch;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Immutable
public class PojoFieldNative extends PojoField {
    private final JdbcType jdbcType;

    private PojoFieldNative(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull JdbcType jdbcType) {
        super(parent, field);
        this.jdbcType = jdbcType;
    }

    public static @NotNull PojoField ofNative(@NotNull ModelField field, @NotNull JdbcType jdbcType) {
        return new PojoFieldNative(PojoParent.DETACHED, field, jdbcType);
    }

    public static @NotNull PojoField ofEnum(@NotNull Class<?> type) {
        assert type.isEnum() : "Type is not an enum: %s".formatted(type);
        ModelField field = new ModelField("ord", "ordinal", "ord", type, type);
        return ofNative(field, JdbcType.Int);
    }

    @Override
    public boolean isNativelySupported() {
        return true;
    }

    public @NotNull JdbcType jdbcType() {
        return jdbcType;
    }

    public @NotNull Column column() {
        return new Column(fullSqlName(), new ColumnType(jdbcType));
    }

    @Override
    public @NotNull AdapterApi adapterInfo() {
        throw new IllegalStateException("Internal error. Native field does not have an adapter: " + this);
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return new PojoFieldNative(parent, field, jdbcType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldNative that && super.equals(obj) && Objects.equals(this.jdbcType, that.jdbcType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jdbcType);
    }

    @Override
    public String toString() {
        return "PojoFieldNative[parent=%s, field=%s, type=%s]".formatted(parent, field, jdbcType);
    }
}
