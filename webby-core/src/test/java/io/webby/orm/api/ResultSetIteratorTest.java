package io.webby.orm.api;

import com.google.common.collect.Lists;
import io.webby.orm.api.debug.DebugSql;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("sql")
public class ResultSetIteratorTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties();

    @SuppressWarnings("ConstantConditions")
    @Test
    public void hasNext() throws Exception {
        try (Statement statement = SQL.connection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT 1, 2")) {
                try (ResultSetIterator<String> iterator = ResultSetIterator.of(resultSet, this::rowToString)) {
                    assertTrue(iterator.hasNext());
                    assertTrue(iterator.hasNext());
                    assertTrue(iterator.hasNext());
                    assertTrue(iterator.hasNext());

                    assertEquals("1.2", iterator.next());
                    assertFalse(iterator.hasNext());
                    assertFalse(iterator.hasNext());
                    assertFalse(iterator.hasNext());
                }
            }
        }
    }

    @Test
    public void statement_closed() throws Exception {
        try (Statement statement = SQL.connection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT 1, 2, 3")) {
                try (ResultSetIterator<String> iterator = ResultSetIterator.of(resultSet, this::rowToString)) {
                    assertFalse(resultSet.isClosed());
                    assertThat(Lists.newArrayList(iterator)).containsExactly("1.2.3");
                    assertFalse(statement.isClosed());
                }
                assertTrue(resultSet.isClosed());
            }
            assertTrue(statement.isClosed());
        }
    }

    @Test
    public void prepared_statement_closed() throws Exception {
        try (PreparedStatement prepared = SQL.connection().prepareStatement("SELECT 1, 2, 3, 4")) {
            try (ResultSet resultSet = prepared.executeQuery()) {
                try (ResultSetIterator<String> iterator = ResultSetIterator.of(resultSet, this::rowToString)) {
                    assertFalse(resultSet.isClosed());
                    assertThat(Lists.newArrayList(iterator)).containsExactly("1.2.3.4");
                    assertFalse(prepared.isClosed());
                }
                assertTrue(resultSet.isClosed());
            }
            assertTrue(prepared.isClosed());
        }
    }

    private @NotNull String rowToString(@NotNull ResultSet row) throws SQLException {
        return DebugSql.toDebugRow(row).values().stream().map(DebugSql.RowValue::strValue).collect(Collectors.joining("."));
    }
}
