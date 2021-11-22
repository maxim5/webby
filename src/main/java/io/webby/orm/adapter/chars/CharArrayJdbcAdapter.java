package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;

@JdbcAdapt(char[].class)
public class CharArrayJdbcAdapter implements JdbcSingleValueAdapter<char[]> {
    public static final CharArrayJdbcAdapter ADAPTER = new CharArrayJdbcAdapter();

    public char[] createInstance(String value) {
        return value.toCharArray();
    }

    @Override
    public Object toValueObject(char[] instance) {
        return String.valueOf(instance);
    }
}
