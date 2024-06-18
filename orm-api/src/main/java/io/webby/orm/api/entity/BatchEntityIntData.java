package io.webby.orm.api.entity;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.Contextual;
import io.spbx.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link BatchEntityData} implementation which stores the only-int row set using a single {@link IntContainer} and
 * splits it into {@link IntArrayList} chunks.
 * <p>
 * The statement is updated via {@link PreparedStatement#setInt(int, int)} from index 0.
 */
public record BatchEntityIntData(@NotNull List<Column> columns,
                                 @NotNull IntContainer values) implements BatchEntityData<IntArrayList> {
    public BatchEntityIntData {
        assert !columns.isEmpty() : "Entity data batch columns are empty: columns=%s, data=%s".formatted(columns, values);
        assert !values.isEmpty() : "Entity data batch empty: columns=%s, data=%s".formatted(columns, values);
        assert values.size() % columns.size() == 0 :
            "Entity values don't match the columns size: columns=%s, data=%s".formatted(columns, values);
    }

    @Override
    public void provideBatchValues(@NotNull PreparedStatement statement,
                                   @Nullable Contextual<?, IntArrayList> contextual) throws SQLException {
        int dataSize = dataSize();
        EasyHppc.iterateChunks(values, dataSize, chunk -> {
            QueryRunner.setPreparedParams(statement, chunk);
            if (contextual != null) {
                contextual.resolveQueryArgs(chunk).setPreparedParams(statement, /*index=*/ dataSize);
            }
            statement.addBatch();
        });
    }
}
