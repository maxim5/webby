package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class InstantJdbcAdapter implements JdbcSingleValueAdapter<Instant>, ResultSetIterator.Converter<Instant> {
    public static final InstantJdbcAdapter ADAPTER = new InstantJdbcAdapter();

    public @NotNull Instant createInstance(@NotNull Timestamp value) {
        return value.toInstant();
    }

    @Override
    public @NotNull Timestamp toValueObject(@NotNull Instant instance) {
        return Timestamp.from(instance);
    }

    @Override
    public @NotNull Instant apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTimestamp(1));
    }
}
