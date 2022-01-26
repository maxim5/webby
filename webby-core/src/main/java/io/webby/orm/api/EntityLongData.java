package io.webby.orm.api;

import com.carrotsearch.hppc.LongContainer;
import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record EntityLongData(@NotNull List<Column> columns, @NotNull LongContainer values) implements EntityData {
    @Override public @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider() {
        return statement -> QueryRunner.setPreparedParams(statement, values);
    }
}
