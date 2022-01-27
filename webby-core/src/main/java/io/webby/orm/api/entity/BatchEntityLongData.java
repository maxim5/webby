package io.webby.orm.api.entity;

import com.carrotsearch.hppc.LongContainer;
import com.carrotsearch.hppc.cursors.LongCursor;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record BatchEntityLongData(@NotNull List<Column> columns,
                                  @NotNull LongContainer values,
                                  int batchSize) implements BatchEntityData {
    public BatchEntityLongData {
        assert !columns.isEmpty() : "Entity data batch columns are empty: " + this;
        assert !values.isEmpty() : "Entity data batch empty: " + this;
        assert columns.size() * batchSize == values.size() : "Entity values don't match the columns and batch size: " + this;
    }

    public BatchEntityLongData(@NotNull List<Column> columns, @NotNull LongContainer values) {
        this(columns, values, columns.isEmpty() ? 0 : values.size() / columns.size());
    }

    @Override
    public void provideValues(@NotNull PreparedStatement statement) throws SQLException {
        setBatchPreparedParams(statement, values, batchSize);
    }

    public static void setBatchPreparedParams(@NotNull PreparedStatement statement,
                                              @NotNull LongContainer values,
                                              int batchSize) throws SQLException {
        int index = 0;
        for (LongCursor cursor : values) {
            statement.setLong(++index, cursor.value);
            if (index % batchSize == 0) {
                statement.addBatch();
            }
        }
    }
}
