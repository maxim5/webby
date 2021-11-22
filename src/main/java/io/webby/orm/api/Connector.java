package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface Connector {
    @NotNull Connection connection();

    @NotNull QueryRunner runner();
}
