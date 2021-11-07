package io.webby.util.sql.adapter.time;

import io.webby.util.sql.adapter.JdbcSingleValueAdapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeJdbcAdapter implements JdbcSingleValueAdapter<LocalDateTime> {
    public static final LocalDateTimeJdbcAdapter ADAPTER = new LocalDateTimeJdbcAdapter();

    public LocalDateTime createInstance(Timestamp value) {
        return value.toLocalDateTime();
    }

    @Override
    public Object toValueObject(LocalDateTime instance) {
        return Timestamp.valueOf(instance);
    }
}
