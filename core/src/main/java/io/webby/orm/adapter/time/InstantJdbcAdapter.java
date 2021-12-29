package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.sql.Timestamp;
import java.time.Instant;

public class InstantJdbcAdapter implements JdbcSingleValueAdapter<Instant> {
    public static final InstantJdbcAdapter ADAPTER = new InstantJdbcAdapter();

    public Instant createInstance(Timestamp value) {
        return value.toInstant();
    }

    @Override
    public Object toValueObject(Instant instance) {
        return Timestamp.from(instance);
    }
}
