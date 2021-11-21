package io.webby.util.sql.api;

import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public enum Engine {
    MsSqlServer,
    MySQL,
    Oracle,
    PostgreSQL,
    SQLite,
    Sybase,
    Unknown;

    public static @NotNull Engine safeFrom(@NotNull Connection connection) {
        try {
            return from(connection);
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }

    // From https://stackoverflow.com/questions/9320200/inline-blob-binary-data-types-in-sql-jdbc/58736912#58736912
    public static @NotNull Engine from(@NotNull Connection connection) throws SQLException {
        String databaseName = connection.getMetaData().getDatabaseProductName();
        return switch (databaseName) {
            case "Microsoft SQL Server" -> MsSqlServer;
            case "Sybase Anywhere", "ASE", "Adaptive Server Enterprise" -> Sybase;
            case "Oracle" -> Oracle;
            case "PostgreSQL" -> PostgreSQL;
            case "MySQL" -> MySQL;
            case "SQLite" -> SQLite;
            default -> Unknown;
        };
    }
}
