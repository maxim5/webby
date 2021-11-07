package io.webby.util.sql.schema;

import com.google.errorprone.annotations.Immutable;
import io.webby.util.lazy.AtomicLazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Immutable
public class PojoFieldAdapter extends PojoField {
    private final Class<?> adapter;
    private final AdapterInfo adapterInfo;
    private final AtomicLazy<List<Column>> columnsRef = new AtomicLazy<>();

    protected PojoFieldAdapter(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull Class<?> adapter) {
        super(parent, field);
        this.adapterInfo = AdapterInfo.ofClass(adapter);
        this.adapter = adapter;
    }

    public static @NotNull PojoField ofAdapter(@NotNull ModelField field, @NotNull Class<?> adapter) {
        return new PojoFieldAdapter(PojoParent.DETACHED, field, adapter);
    }

    public @NotNull Class<?> adapterClass() {
        return adapter;
    }

    @Override
    public @NotNull AdapterInfo adapterInfo() {
        return adapterInfo;
    }

    public @NotNull List<Column> columns() {
        return columnsRef.lazyGet(() -> adapterInfo.adapterColumns(fullSqlName()));
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return new PojoFieldAdapter(parent, field, adapter);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldAdapter that && super.equals(obj) && Objects.equals(this.adapterInfo, that.adapterInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adapterInfo);
    }

    @Override
    public String toString() {
        return "PojoFieldAdapter[parent=%s, field=%s, adapter=%s]".formatted(parent, field, adapterInfo);
    }
}
