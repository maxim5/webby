package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LocalDateJdbcAdapter implements JdbcSingleValueAdapter<LocalDate>, ResultSetIterator.Converter<LocalDate> {
    public static final LocalDateJdbcAdapter ADAPTER = new LocalDateJdbcAdapter();

    public @NotNull LocalDate createInstance(@NotNull Date value) {
        return value.toLocalDate();
    }

    @Override
    public @NotNull Date toValueObject(@NotNull LocalDate instance) {
        return Date.valueOf(instance);
    }

    @Override
    public @NotNull LocalDate apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getDate(1));
    }
}
