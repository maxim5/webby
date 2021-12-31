package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcSingleValueAdapter;

import java.time.Period;

public class PeriodJdbcAdapter implements JdbcSingleValueAdapter<Period> {
    public static final PeriodJdbcAdapter ADAPTER = new PeriodJdbcAdapter();

    public Period createInstance(String value) {
        return Period.parse(value);
    }

    @Override
    public Object toValueObject(Period instance) {
        return instance.toString();
    }
}
