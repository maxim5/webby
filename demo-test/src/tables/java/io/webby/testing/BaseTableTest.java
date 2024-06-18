package io.webby.testing;

import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Engine;
import io.spbx.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public interface BaseTableTest<E, T extends BaseTable<E>> extends TableTestApi<E, T> {
    @Test
    default void column_names() {
        String tableName = table().meta().sqlTableName();
        List<String> expectedColumns = parseColumnNamesFromDb(tableName);
        List<String> columnNames = table().meta().sqlColumns().stream().map(TableMeta.ColumnMeta::name).toList();
        assertThat(columnNames).containsExactlyElementsIn(expectedColumns);
    }

    @Test
    default void empty() {
        assertThat(table().count()).isEqualTo(0);
        assertThat(table().isEmpty()).isTrue();
        assertThat(table().isNotEmpty()).isFalse();
        assertThat(table().fetchAll()).isEmpty();
    }

    List<String> parseColumnNamesFromDb(@NotNull String name);

    default void assumeOneOfEngines(@NotNull Engine ... engines) {
        assumeTrue(table().engine().isOneOf(engines),
                   "Can't run the test because engine is not supported: %s".formatted(table().engine()));
    }

    default void assumeNoneOfEngines(@NotNull Engine ... engines) {
        assumeFalse(table().engine().isOneOf(engines),
                    "Can't run the test because engine is not supported: %s".formatted(table().engine()));
    }
}
