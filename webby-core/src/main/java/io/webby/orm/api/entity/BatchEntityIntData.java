package io.webby.orm.api.entity;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record BatchEntityIntData(@NotNull List<Column> columns,
                                 @NotNull IntContainer values,
                                 int batchSize) implements BatchEntityData {
    public BatchEntityIntData {
        assert !columns.isEmpty() : "Entity data batch columns are empty: " + this;
        assert !values.isEmpty() : "Entity data batch empty: " + this;
        assert columns.size() * batchSize == values.size() : "Entity values don't match the columns and batch size: " + this;
    }

    public BatchEntityIntData(@NotNull List<Column> columns, @NotNull IntContainer values) {
        this(columns, values, columns.isEmpty() ? 0 : values.size() / columns.size());
    }

    @Override
    public @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider() {
        return statement -> setBatchPreparedParams(statement, values, batchSize);
    }

    public static void setBatchPreparedParams(@NotNull PreparedStatement statement,
                                              @NotNull IntContainer values,
                                              int batchSize) throws SQLException {
        int index = 0;
        for (IntCursor cursor : values) {
            statement.setInt(++index, cursor.value);
            if (index % batchSize == 0) {
                statement.addBatch();
            }
        }
    }
}
