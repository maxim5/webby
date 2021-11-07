package io.webby.util.sql.adapter.time;

import io.webby.util.sql.adapter.JdbcSingleValueAdapter;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeJdbcAdapter implements JdbcSingleValueAdapter<ZonedDateTime> {
    public static final ZonedDateTimeJdbcAdapter ADAPTER = new ZonedDateTimeJdbcAdapter();

    public ZonedDateTime createInstance(Timestamp value) {
        return value.toLocalDateTime().atZone(ZoneId.systemDefault());
    }

    @Override
    public Object toValueObject(ZonedDateTime instance) {
        return Timestamp.valueOf(instance.toLocalDateTime());
    }
}
