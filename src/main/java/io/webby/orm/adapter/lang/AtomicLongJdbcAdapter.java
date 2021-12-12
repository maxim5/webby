package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.util.concurrent.atomic.AtomicLong;

@JdbcAdapt(AtomicLong.class)
public class AtomicLongJdbcAdapter implements JdbcSingleValueAdapter<AtomicLong> {
    public static final AtomicLongJdbcAdapter ADAPTER = new AtomicLongJdbcAdapter();

    public AtomicLong createInstance(long value) {
        return new AtomicLong(value);
    }

    @Override
    public Object toValueObject(AtomicLong instance) {
        return instance.get();
    }
}
