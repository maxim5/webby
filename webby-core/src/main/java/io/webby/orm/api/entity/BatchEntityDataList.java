package io.webby.orm.api.entity;

import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public record BatchEntityDataList(@NotNull List<EntityData> batch) implements BatchEntityData {
    public BatchEntityDataList {
        assert !batch.isEmpty() : "Entity batch is empty: " + batch;
    }

    @Override
    public @NotNull Collection<? extends Column> columns() {
        return batch.get(0).columns();
    }

    @Override
    public void provideValues(@NotNull PreparedStatement statement) throws SQLException {
        for (EntityData entityData : batch) {
            entityData.provideValues(statement);
            statement.addBatch();
        }
    }

    @Override
    public int batchSize() {
        return batch.size();
    }
}
