package io.webby.orm.api.entity;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.Contextual;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record BatchEntityIntData(@NotNull List<Column> columns,
                                 @NotNull IntContainer values) implements BatchEntityData<IntArrayList> {
    public BatchEntityIntData {
        assert !columns.isEmpty() : "Entity data batch columns are empty: " + this;
        assert !values.isEmpty() : "Entity data batch empty: " + this;
        assert values.size() % columns.size() == 0 : "Entity values don't match the columns size: " + this;
    }

    @Override
    public void provideBatchValues(@NotNull PreparedStatement statement,
                                   @Nullable Contextual<?, IntArrayList> contextual) throws SQLException {
        int dataSize = dataSize();
        EasyHppc.iterateChunks(values, dataSize, chunk -> {
            QueryRunner.setPreparedParams(statement, chunk);
            if (contextual != null) {
                contextual.resolveQueryArgs(chunk).setPreparedParams(statement, dataSize);
            }
            statement.addBatch();
        });
    }
}
