package io.webby.db.kv.mapdb;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

public interface MapDbCreator {
    @Nullable
    DB.Maker<HTreeMap<?,?>> getMaker(@NotNull DB db, @NotNull String name, @NotNull Class<?> key, @NotNull Class<?> value);
}
