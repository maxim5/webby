package io.webby.testing;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface BaseTableTest<E, T extends BaseTable<E>> extends TableTestApi<E, T> {
    @Test
    default void column_names() throws Exception {
        String tableName = table().meta().sqlTableName();
        List<String> expectedColumns = parseColumnNamesFromDb(tableName);
        List<String> columnNames = table().meta().sqlColumns().stream().map(TableMeta.ColumnMeta::name).toList();
        assertThat(columnNames).containsExactlyElementsIn(expectedColumns);
    }

    @Test
    default void empty() {
        assertEquals(0, table().count());
        assertTrue(table().isEmpty());
        assertFalse(table().isNotEmpty());
        assertThat(table().fetchAll()).isEmpty();
    }

    List<String> parseColumnNamesFromDb(@NotNull String name) throws SQLException;
}
