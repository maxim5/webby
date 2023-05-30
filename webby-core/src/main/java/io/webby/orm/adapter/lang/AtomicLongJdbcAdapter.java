package io.webby.orm.adapter.lang;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

@JdbcAdapt(AtomicLong.class)
public class AtomicLongJdbcAdapter implements JdbcSingleValueAdapter<AtomicLong>, ResultSetIterator.Converter<AtomicLong> {
    public static final AtomicLongJdbcAdapter ADAPTER = new AtomicLongJdbcAdapter();

    public @NotNull AtomicLong createInstance(long value) {
        return new AtomicLong(value);
    }

    @Override
    public @NotNull Long toValueObject(@NotNull AtomicLong instance) {
        return instance.get();
    }

    @Override
    public @NotNull AtomicLong apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getLong(1));
    }
}
