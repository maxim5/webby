package io.webby.orm.api.entity;

import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Set;

/**
 * A {@link EntityData} implementation which stores the arbitrary row using a {@link EnumMap}.
 * The statement is updated via {@link PreparedStatement#setObject(int, Object)} from index 0.
 */
public record EntityColumnMap<E extends Enum<E> & Column>(@NotNull EnumMap<E, Object> map) implements EntityData<EnumMap<E, Object>> {
    public EntityColumnMap {
        assert !map.isEmpty() : "Entity data is empty: " + map;
    }

    @Override
    public @NotNull Set<E> columns() {
        return map.keySet();
    }

    @Override
    public @NotNull EnumMap<E, Object> data() {
        return map;
    }

    @Override
    public int provideValues(@NotNull PreparedStatement statement) throws SQLException {
        return QueryRunner.setPreparedParams(statement, map.values());
    }
}
