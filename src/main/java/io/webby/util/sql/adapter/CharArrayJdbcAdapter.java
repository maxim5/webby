package io.webby.util.sql.adapter;

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
