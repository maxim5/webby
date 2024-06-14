package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

@JdbcAdapt(AtomicInteger.class)
public class AtomicIntegerJdbcAdapter implements JdbcSingleValueAdapter<AtomicInteger>, ResultSetIterator.Converter<AtomicInteger> {
    public static final AtomicIntegerJdbcAdapter ADAPTER = new AtomicIntegerJdbcAdapter();

    public @NotNull AtomicInteger createInstance(int value) {
        return new AtomicInteger(value);
    }

    @Override
    public @NotNull Integer toValueObject(@NotNull AtomicInteger instance) {
        return instance.get();
    }

    @Override
    public @NotNull AtomicInteger apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getInt(1));
    }
}
