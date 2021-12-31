package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

@JdbcAdapt(AtomicBoolean.class)
public class AtomicBooleanJdbcAdapter implements JdbcSingleValueAdapter<AtomicBoolean> {
    public static final AtomicBooleanJdbcAdapter ADAPTER = new AtomicBooleanJdbcAdapter();

    public AtomicBoolean createInstance(boolean value) {
        return new AtomicBoolean(value);
    }

    @Override
    public Object toValueObject(AtomicBoolean instance) {
        return instance.get();
    }
}
