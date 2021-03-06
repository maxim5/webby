package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;

@JdbcAdapt(CharSequence.class)
public class CharSequenceJdbcAdapter implements JdbcSingleValueAdapter<CharSequence> {
    public static final CharSequenceJdbcAdapter ADAPTER = new CharSequenceJdbcAdapter();

    public CharSequence createInstance(String value) {
        return value;
    }

    @Override
    public Object toValueObject(CharSequence instance) {
        return instance.toString();
    }
}
