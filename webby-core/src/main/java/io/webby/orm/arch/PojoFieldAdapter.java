package io.webby.orm.arch;

import com.google.errorprone.annotations.Immutable;
import io.webby.util.lazy.AtomicCacheCompute;
import io.webby.util.lazy.CacheCompute;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Immutable
public class PojoFieldAdapter extends PojoField {
    private final Class<?> adapter;
    private final AdapterApi adapterApi;
    private final CacheCompute<List<Column>> columnsRef = AtomicCacheCompute.createEmpty();

    protected PojoFieldAdapter(@NotNull PojoParent parent, @NotNull ModelField field, @NotNull Class<?> adapter) {
        super(parent, field);
        this.adapterApi = AdapterApi.ofClass(adapter);
        this.adapter = adapter;
    }

    public static @NotNull PojoField ofAdapter(@NotNull ModelField field, @NotNull Class<?> adapter) {
        return new PojoFieldAdapter(PojoParent.DETACHED, field, adapter);
    }

    public @NotNull Class<?> adapterClass() {
        return adapter;
    }

    @Override
    public @NotNull AdapterApi adapterInfo() {
        return adapterApi;
    }

    public @NotNull List<Column> columns() {
        return columnsRef.getOrCompute(() -> adapterApi.adapterColumns(fullSqlName()));
    }

    @Override
    public @NotNull PojoField reattachedTo(@NotNull PojoParent parent) {
        return new PojoFieldAdapter(parent, field, adapter);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PojoFieldAdapter that && super.equals(obj) && Objects.equals(this.adapterApi, that.adapterApi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), adapterApi);
    }

    @Override
    public String toString() {
        return "PojoFieldAdapter[parent=%s, field=%s, adapter=%s]".formatted(parent, field, adapterApi);
    }
}
