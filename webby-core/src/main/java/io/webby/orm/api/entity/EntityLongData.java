package io.webby.orm.api.entity;

import com.carrotsearch.hppc.LongContainer;
import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record EntityLongData(@NotNull List<Column> columns, @NotNull LongContainer data) implements EntityData<LongContainer> {
    public EntityLongData {
        assert !columns.isEmpty() : "Entity data is empty: " + this;
        assert columns.size() == data.size() : "Entity columns do not match the data: " + this;
    }

    @Override
    public void provideValues(@NotNull PreparedStatement statement) throws SQLException {
        QueryRunner.setPreparedParams(statement, data);
    }
}
