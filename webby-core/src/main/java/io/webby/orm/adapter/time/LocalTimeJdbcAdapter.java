package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.sql.Time;
import java.time.LocalTime;

public class LocalTimeJdbcAdapter implements JdbcSingleValueAdapter<LocalTime> {
    public static final LocalTimeJdbcAdapter ADAPTER = new LocalTimeJdbcAdapter();

    public LocalTime createInstance(Time value) {
        return value.toLocalTime();
    }

    @Override
    public Object toValueObject(LocalTime instance) {
        return Time.valueOf(instance);
    }
}
