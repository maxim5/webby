package io.webby.orm.api.entity;

import com.carrotsearch.hppc.IntContainer;
import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link EntityData} implementation which stores the only-integers row using a {@link IntContainer}.
 * The statement is updated via {@link PreparedStatement#setInt(int, int)} from index 0.
 */
public record EntityIntData(@NotNull List<Column> columns, @NotNull IntContainer data) implements EntityData<IntContainer> {
    public EntityIntData {
        assert !columns.isEmpty() : "Entity data is empty: " + this;
        assert columns.size() == data.size() : "Entity columns do not match the data: " + this;
    }

    @Override
    public int provideValues(@NotNull PreparedStatement statement) throws SQLException {
        return QueryRunner.setPreparedParams(statement, data);
    }
}
