package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.sql.Date;
import java.time.LocalDate;

public class LocalDateJdbcAdapter implements JdbcSingleValueAdapter<LocalDate> {
    public static final LocalDateJdbcAdapter ADAPTER = new LocalDateJdbcAdapter();

    public LocalDate createInstance(Date value) {
        return value.toLocalDate();
    }

    @Override
    public Object toValueObject(LocalDate instance) {
        return Date.valueOf(instance);
    }
}
