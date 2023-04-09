package io.webby.orm.api;

import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the type of JDBC engine or DBMS.
 * A JDBC {@link java.sql.Connection} has exactly one {@link Engine}.
 * Unsupported engines are represented as {@link #Unknown}.
 */
public enum Engine {
    Derby("derby"),
    H2("h2"),
    HyperSQL("hsqldb"),
    MariaDB("mariadb"),
    MsSqlServer("sqlserver"),
    MySQL("mysql"),
    Oracle("oracle"),
    PostgreSQL("postgresql"),
    SQLite("sqlite"),
    Sybase("sybase"),
    Unknown("");

    private final String jdbcType;

    Engine(@NotNull String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public @NotNull String jdbcType() {
        return jdbcType;
    }

    private static final Map<String, Engine> ENGINE_MAP =
            Arrays.stream(Engine.values()).collect(Collectors.toMap(Engine::jdbcType, value -> value));

    public static @NotNull Engine fromJdbcType(@NotNull String jdbcType) {
        return ENGINE_MAP.getOrDefault(jdbcType, Unknown);
    }

    public static @NotNull Engine fromConnectionSafe(@NotNull Connection connection) {
        try {
            return fromConnection(connection);
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }

    // From https://stackoverflow.com/questions/9320200/inline-blob-binary-data-types-in-sql-jdbc/58736912#58736912
    public static @NotNull Engine fromConnection(@NotNull Connection connection) throws SQLException {
        String databaseName = connection.getMetaData().getDatabaseProductName();
        return switch (databaseName) {
            case "Apache Derby" -> Derby;
            case "H2" -> H2;
            case "HSQL Database Engine" -> HyperSQL;
            case "Microsoft SQL Server" -> MsSqlServer;
            case "MariaDB" -> MariaDB;
            case "MySQL" -> MySQL;
            case "Oracle" -> Oracle;
            case "PostgreSQL" -> PostgreSQL;
            case "SQLite" -> SQLite;
            case "Sybase Anywhere", "ASE", "Adaptive Server Enterprise" -> Sybase;
            default -> Unknown;
        };
    }
}
