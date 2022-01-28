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
        IntArrayList arrayList = EasyHppc.toArrayList(values);
        for (int dataSize = dataSize(), i = 0; i < arrayList.size(); i += dataSize) {
            IntArrayList slice = EasyHppc.slice(arrayList, i, i + dataSize);
            QueryRunner.setPreparedParams(statement, slice);
            if (contextual != null) {
                List<Object> restQueryArgs = contextual.resolveQueryArgs(slice);
                QueryRunner.setPreparedParams(statement, restQueryArgs, dataSize);
            }
            statement.addBatch();
        }
    }
}
