package io.spbx.orm.adapter.time;

import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

public class DurationJdbcAdapter implements JdbcSingleValueAdapter<Duration>, ResultSetIterator.Converter<Duration> {
    public static final DurationJdbcAdapter ADAPTER = new DurationJdbcAdapter();

    public @NotNull Duration createInstance(long value) {
        return Duration.ofNanos(value);
    }

    @Override
    public @NotNull Long toValueObject(@NotNull Duration instance) {
        return instance.toNanos();
    }

    @Override
    public @NotNull Duration apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getLong(1));
    }
}
