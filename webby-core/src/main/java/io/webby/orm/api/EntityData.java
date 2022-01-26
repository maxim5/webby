package io.webby.orm.api;

import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public interface EntityData {
    @NotNull Collection<? extends Column> columns();

    @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider();
}
