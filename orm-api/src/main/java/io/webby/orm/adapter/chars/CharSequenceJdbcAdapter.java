package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

@JdbcAdapt(CharSequence.class)
public class CharSequenceJdbcAdapter implements JdbcSingleValueAdapter<CharSequence>, ResultSetIterator.Converter<CharSequence> {
    public static final CharSequenceJdbcAdapter ADAPTER = new CharSequenceJdbcAdapter();

    public @NotNull CharSequence createInstance(@NotNull String value) {
        return value;
    }

    @Override
    public @NotNull String toValueObject(@NotNull CharSequence instance) {
        return instance.toString();
    }

    @Override
    public @NotNull CharSequence apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getString(1));
    }
}
