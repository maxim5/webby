package io.webby.orm.api.entity;

import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Set;

public record EntityColumnMap<E extends Enum<E> & Column>(@NotNull EnumMap<E, Object> map) implements EntityData {
    public EntityColumnMap {
        assert !map.isEmpty() : "Entity data is empty: " + map;
    }

    @Override
    public @NotNull Set<E> columns() {
        return map.keySet();
    }

    @Override
    public @NotNull ThrowConsumer<PreparedStatement, SQLException> dataProvider() {
        return statement -> QueryRunner.setPreparedParams(statement, map.values());
    }
}
