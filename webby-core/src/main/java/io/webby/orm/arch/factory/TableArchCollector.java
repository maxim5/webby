package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableMap;
import io.webby.orm.arch.field.TableArch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.webby.orm.arch.InvalidSqlModelException.assure;
import static java.util.Objects.requireNonNull;

public class TableArchCollector {
    private final Map<Class<?>, TableArch> tables = new LinkedHashMap<>();

    public @NotNull ImmutableMap<Class<?>, TableArch> getAllTables() {
        return ImmutableMap.copyOf(tables);
    }

    public @NotNull Collection<TableArch> getTableArches() {
        return tables.values();
    }

    public void putTable(@NotNull Class<?> key, @NotNull TableArch table) {
        assure(tables.put(key, table) == null, "Duplicate input: " + key);
    }

    public @Nullable TableArch getTable(@NotNull Class<?> key) {
        return tables.get(key);
    }

    public @NotNull TableArch getTableOrDie(@NotNull Class<?> key) {
        return requireNonNull(tables.get(key));
    }
}
