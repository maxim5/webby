package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface Connector {
    @NotNull Connection connection();

    default @NotNull QueryRunner runner() {
        return new QueryRunner(connection());
    }

    default @NotNull Engine engine() {
        return Engine.fromConnectionSafe(connection());
    }
}
