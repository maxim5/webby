package io.webby.testing;

import io.webby.util.sql.api.BaseTable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface BaseTableTest<E, T extends BaseTable<E>> extends TableTestApi<E, T> {
    @Test
    default void column_names() throws Exception {
        String tableName = table().meta().sqlTableName();
        List<String> expectedColumns = parseColumnNamesFromDb(tableName);
        assertThat(table().meta().sqlColumns()).containsExactlyElementsIn(expectedColumns);
    }

    @Test
    default void empty() {
        assertEquals(0, table().count());
        assertTrue(table().isEmpty());
        assertThat(table().fetchAll()).isEmpty();
    }

    List<String> parseColumnNamesFromDb(@NotNull String name) throws SQLException;
}