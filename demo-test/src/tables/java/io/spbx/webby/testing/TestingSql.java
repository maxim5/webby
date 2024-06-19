package io.spbx.webby.testing;

import io.spbx.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

import static com.google.common.truth.Truth.assertThat;

public class TestingSql {
    public static <K, E, T extends TableObj<K, E>> @NotNull E getOrInsert(@NotNull T table, @NotNull E entity) {
        E existing = table.getByPkOrNull(table.keyOf(entity));
        if (existing == null) {
            assertThat(table.insert(entity)).isEqualTo(1);
            return entity;
        }
        return existing;
    }
}
