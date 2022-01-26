package io.webby.orm.api;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record EntityIntData(@NotNull List<Column> columns, @NotNull IntContainer values) implements EntityData {
    @Override public @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider() {
        return statement -> setParams(statement, values);
    }

    public static void setParams(@NotNull PreparedStatement statement, @NotNull IntContainer values) throws SQLException {
        int index = 0;
        for (IntCursor cursor : values) {
            statement.setInt(++index, cursor.value);
        }
    }
}
