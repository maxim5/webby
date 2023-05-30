package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeJdbcAdapter implements JdbcSingleValueAdapter<ZonedDateTime>, ResultSetIterator.Converter<ZonedDateTime> {
    public static final ZonedDateTimeJdbcAdapter ADAPTER = new ZonedDateTimeJdbcAdapter();

    public @NotNull ZonedDateTime createInstance(@NotNull Timestamp value) {
        return value.toLocalDateTime().atZone(ZoneId.systemDefault());
    }

    @Override
    public @NotNull Timestamp toValueObject(@NotNull ZonedDateTime instance) {
        return Timestamp.valueOf(instance.toLocalDateTime());
    }

    @Override
    public @NotNull ZonedDateTime apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getTimestamp(1));
    }
}
