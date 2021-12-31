package io.webby.testing;

import io.webby.orm.api.Connector;
import io.webby.orm.api.debug.DebugSql;
import io.webby.orm.api.TableObj;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("sql")
public abstract class SqlDbTableTest<K, E, T extends TableObj<K, E>> implements BaseTableTest<E, T> {
    @RegisterExtension private final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    protected T table;

    @Override
    public @NotNull T table() {
        return table;
    }

    @BeforeEach
    void setUp() throws Exception {
        setUp(connector());
        SQL_DB.savepoint();
    }

    protected abstract void setUp(@NotNull Connector connector) throws Exception;

    @Test
    public void engine() {
        assertEquals(SQL_DB.engine(), table.engine());
    }

    public @NotNull List<String> parseColumnNamesFromDb(@NotNull String name) {
        return switch (SQL_DB.engine()) {
            case SQLite -> {
                String sql = descTableInSqlite(name)
                        .replaceAll("\\s+", " ")
                        .replaceFirst(".*\\((.*?)\\)", "$1");
                yield Arrays.stream(sql.split(","))
                        .map(String::trim)
                        .map(line -> line.replaceFirst("(\\w+) .*", "$1"))
                        .toList();
            }
            case H2, MySQL -> SQL_DB.runQuery("SHOW COLUMNS FROM " + name).stream()
                    .map(row -> row.getValueAt(0))
                    .map(DebugSql.RowValue::strValue)
                    .map(String::toLowerCase)
                    .toList();
            default -> throw new UnsupportedOperationException("Engine not supported: " + SQL_DB.engine());
        };
    }

    private @NotNull String descTableInSqlite(@NotNull String name) {
        List<DebugSql.Row> rows = SQL_DB.runQuery("SELECT sql FROM sqlite_master WHERE name=?", name);
        assertFalse(rows.isEmpty(), "SQL table not found in DB: %s".formatted(name));
        assertThat(rows).hasSize(1);
        return rows.get(0).findValue("sql").map(DebugSql.RowValue::strValue).orElseThrow();
    }

    protected @NotNull Connector connector() {
        return SQL_DB;
    }
}
