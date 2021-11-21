package io.webby.testing;

import io.webby.db.sql.SqlSettings;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.util.sql.api.DebugSql;
import io.webby.util.sql.api.Engine;
import io.webby.util.sql.api.QueryRunner;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class SqliteTableTest<K, E, T extends TableObj<K, E>> implements BaseTableTest<E, T> {
    @RegisterExtension private final SqlDbSetupExtension SQL_DB = new SqlDbSetupExtension(SqlSettings.SQLITE_IN_MEMORY);

    protected T table;

    @Override
    public @NotNull T table() {
        return table;
    }

    @BeforeEach
    void setUp() throws Exception {
        setUp(connection());
    }

    protected abstract void setUp(@NotNull Connection connection) throws Exception;

    @Test
    public void engine() {
        assertEquals(Engine.SQLite, table.engine());
    }

    public List<String> parseColumnNamesFromDb(@NotNull String name) throws SQLException {
        ResultSet resultSet = new QueryRunner(connection()).runQuery("SELECT sql FROM sqlite_master WHERE name=?", name);
        List<DebugSql.Row> rows = DebugSql.toDebugRows(resultSet);
        assertFalse(rows.isEmpty(), "SQL table not found in DB: %s".formatted(name));
        assertThat(rows).hasSize(1);

        String sql = rows.get(0).findValue("sql")
                .map(DebugSql.RowValue::value)
                .orElseThrow()
                .replaceAll("\\s+", " ")
                .replaceFirst(".*\\((.*?)\\)", "$1");

        return Arrays.stream(sql.split(","))
                .map(String::trim)
                .map(line -> line.replaceFirst("(\\w+) .*", "$1"))
                .toList();
    }

    protected @NotNull Connection connection() {
        return SQL_DB.getConnection();
    }
}
