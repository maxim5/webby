package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

@JdbcAdapt(AtomicBoolean.class)
public class AtomicBooleanJdbcAdapter implements JdbcSingleValueAdapter<AtomicBoolean>, ResultSetIterator.Converter<AtomicBoolean> {
    public static final AtomicBooleanJdbcAdapter ADAPTER = new AtomicBooleanJdbcAdapter();

    public @NotNull AtomicBoolean createInstance(boolean value) {
        return new AtomicBoolean(value);
    }

    @Override
    public @NotNull Boolean toValueObject(@NotNull AtomicBoolean instance) {
        return instance.get();
    }

    @Override
    public @NotNull AtomicBoolean apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getBoolean(1));
    }
}
