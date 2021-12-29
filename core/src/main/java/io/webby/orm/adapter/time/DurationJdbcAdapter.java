package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.time.Duration;

public class DurationJdbcAdapter implements JdbcSingleValueAdapter<Duration> {
    public static final DurationJdbcAdapter ADAPTER = new DurationJdbcAdapter();

    public Duration createInstance(long value) {
        return Duration.ofNanos(value);
    }

    @Override
    public Object toValueObject(Duration instance) {
        return instance.toNanos();
    }
}
