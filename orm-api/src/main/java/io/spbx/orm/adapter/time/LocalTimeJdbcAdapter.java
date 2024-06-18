package io.spbx.orm.adapter.time;

import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

public class LocalTimeJdbcAdapter implements JdbcSingleValueAdapter<LocalTime>, ResultSetIterator.Converter<LocalTime> {
    public static final LocalTimeJdbcAdapter ADAPTER = new LocalTimeJdbcAdapter();

    public @NotNull LocalTime createInstance(@NotNull Time value) {
        return value.toLocalTime();
    }

    @Override
    public @NotNull Time toValueObject(@NotNull LocalTime instance) {
        return Time.valueOf(instance);
    }

    @Override
    public @NotNull LocalTime apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTime(1));
    }
}
