package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.util.concurrent.atomic.AtomicInteger;

@JdbcAdapt(AtomicInteger.class)
public class AtomicIntegerJdbcAdapter implements JdbcSingleValueAdapter<AtomicInteger> {
    public static final AtomicIntegerJdbcAdapter ADAPTER = new AtomicIntegerJdbcAdapter();

    public AtomicInteger createInstance(int value) {
        return new AtomicInteger(value);
    }

    @Override
    public Object toValueObject(AtomicInteger instance) {
        return instance.get();
    }
}
