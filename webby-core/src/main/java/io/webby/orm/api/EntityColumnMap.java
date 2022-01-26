package io.webby.orm.api;

import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Set;

public record EntityColumnMap<E extends Enum<E> & Column>(@NotNull EnumMap<E, Object> map) implements EntityData {
    @Override
    public @NotNull Set<E> columns() {
        return map.keySet();
    }

    @Override
    public @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider() {
        return statement -> QueryRunner.setParams(statement, map.values());
    }
}
