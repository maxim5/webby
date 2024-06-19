package io.spbx.webby.testing;

import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.Connector;
import io.spbx.orm.api.QueryRunner;
import io.spbx.orm.api.debug.DebugSql;
import io.spbx.webby.testing.ext.SqlDbExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.MoreTruth.assertThat;

@Tag("sql")
public abstract class SqlDbTableTest<E, T extends BaseTable<E>> implements BaseTableTest<E, T> {
    @RegisterExtension private static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withSavepoints();

    protected T table;

    @Override
    public @NotNull T table() {
        return table;
    }

    @BeforeEach
    void setUp() throws Exception {
        setUp(connector());
        SQL.setupTestData();
        fillUp(connector());
    }

    protected abstract void setUp(@NotNull Connector connector) throws Exception;

    protected void fillUp(@NotNull Connector connector) throws Exception {}

    @Test
    public void engine() {
        assertThat(table.engine()).isEqualTo(SQL.engine());
    }

    public @NotNull List<String> parseColumnNamesFromDb(@NotNull String name) {
        return switch (SQL.engine()) {
            case SQLite -> {
                String sql = descTableInSqlite(name)
                    .replaceAll("\\s+", " ")                                    // trim spaces to one
                    .replaceAll("(PRIMARY KEY|UNIQUE|DEFAULT) \\(.*?\\)", "")   // drop constraints
                    .replaceAll("FOREIGN KEY.*REFERENCES .*\\(.*?\\)", "")      // drop FOREIGN KEY
                    .replaceFirst("CREATE TABLE .*\\((.*?)\\)", "$1");          // get what's inside CREATE TABLE
                // Not `sql` has the format "foo INTEGER, bar VARCHAR, ..."
                yield Arrays.stream(sql.split(","))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.replaceFirst("(\\w+) .*", "$1"))
                    .toList();
            }
            case H2, MySQL, MariaDB -> SQL.runQuery("SHOW COLUMNS FROM " + name).stream()
                    .map(row -> row.getValueAt(0))
                    .map(DebugSql.RowValue::strValue)
                    .map(String::toLowerCase)
                    .toList();
            default -> throw new UnsupportedOperationException("Engine not supported: " + SQL.engine());
        };
    }

    private @NotNull String descTableInSqlite(@NotNull String name) {
        List<DebugSql.Row> rows = SQL.runQuery("SELECT sql FROM sqlite_master WHERE name=?", name);
        assertThat(rows.isEmpty()).withMessage("SQL table not found in DB: %s", name).isFalse();
        assertThat(rows).hasSize(1);
        return rows.getFirst().findValue("sql").map(DebugSql.RowValue::strValue).orElseThrow();
    }

    protected @NotNull Connector connector() {
        return SQL;
    }

    protected @NotNull QueryRunner runner() {
        return connector().runner();
    }
}
