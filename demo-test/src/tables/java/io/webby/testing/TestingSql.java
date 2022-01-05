package io.webby.testing;

import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestingSql {
    public static <K, E, T extends TableObj<K, E>> @NotNull E getOrInsert(@NotNull T table, @NotNull E entity) {
        E existing = table.getByPkOrNull(table.keyOf(entity));
        if (existing == null) {
            assertEquals(1, table.insert(entity));
            return entity;
        }
        return existing;
    }
}
