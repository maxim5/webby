package io.webby.orm.api.entity;

import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface EntityData<D> extends ColumnSet {
    @NotNull D data();

    void provideValues(@NotNull PreparedStatement statement) throws SQLException;
}
