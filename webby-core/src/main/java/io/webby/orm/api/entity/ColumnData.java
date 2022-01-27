package io.webby.orm.api.entity;

import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public interface ColumnData {
    @NotNull Collection<? extends Column> columns();

    void provideValues(@NotNull PreparedStatement statement) throws SQLException;
}
