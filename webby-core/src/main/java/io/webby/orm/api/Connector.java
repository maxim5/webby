package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface Connector extends WithRunner, WithEngine {
    @NotNull Connection connection();

    @Override
    default @NotNull QueryRunner runner() {
        return new QueryRunner(connection());
    }

    @Override
    default @NotNull Engine engine() {
        return Engine.fromConnectionSafe(connection());
    }
}
