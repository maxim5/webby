package io.webby.orm.api.entity;

import io.webby.orm.api.query.Contextual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BatchEntityData<B> extends ColumnSet {
    default int dataSize() {
        return columns().size();
    }

    void provideBatchValues(@NotNull PreparedStatement statement,
                            @Nullable Contextual<?, B> contextual) throws SQLException;
}
