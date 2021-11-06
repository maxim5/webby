package io.webby.util.sql.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeJdbcAdapter implements JdbcSingleColumnArrayAdapter<LocalDateTime> {
    public static final LocalDateTimeJdbcAdapter ADAPTER = new LocalDateTimeJdbcAdapter();

    public LocalDateTime createInstance(Timestamp value) {
        return value.toLocalDateTime();
    }

    @Override
    public Object toValueObject(LocalDateTime instance) {
        return Timestamp.valueOf(instance);
    }
}
