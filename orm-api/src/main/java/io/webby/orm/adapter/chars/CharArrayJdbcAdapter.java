package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

@JdbcAdapt(char[].class)
public class CharArrayJdbcAdapter implements JdbcSingleValueAdapter<char[]>, ResultSetIterator.Converter<char[]> {
    public static final CharArrayJdbcAdapter ADAPTER = new CharArrayJdbcAdapter();

    public char @NotNull [] createInstance(@NotNull String value) {
        return value.toCharArray();
    }

    @Override
    public @NotNull String toValueObject(char[] instance) {
        return String.valueOf(instance);
    }

    @Override
    public char @NotNull [] apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getString(1));
    }
}
