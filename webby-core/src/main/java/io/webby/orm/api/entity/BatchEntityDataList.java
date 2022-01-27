package io.webby.orm.api.entity;

import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.Contextual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public record BatchEntityDataList<D>(@NotNull List<EntityData<D>> batch) implements BatchEntityData<D> {
    public BatchEntityDataList {
        assert !batch.isEmpty() : "Entity batch is empty: " + batch;
    }

    @Override
    public @NotNull Collection<? extends Column> columns() {
        return batch.get(0).columns();
    }

    @Override
    public void provideBatchValues(@NotNull PreparedStatement statement,
                                   @Nullable Contextual<?, D> contextual) throws SQLException {
        int dataSize = dataSize();
        for (EntityData<D> entityData : batch) {
            entityData.provideValues(statement);
            if (contextual != null) {
                List<Object> restQueryArgs = contextual.resolveQueryArgs(entityData.data());
                QueryRunner.setPreparedParams(statement, restQueryArgs, dataSize);
            }
            statement.addBatch();
        }
    }
}
