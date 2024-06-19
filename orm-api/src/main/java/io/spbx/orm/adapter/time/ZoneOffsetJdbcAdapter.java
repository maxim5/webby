package io.spbx.orm.adapter.time;

import io.spbx.orm.adapter.JdbcSingleValueAdapter;
import io.spbx.orm.api.ResultSetIterator;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;

public class ZoneOffsetJdbcAdapter implements JdbcSingleValueAdapter<ZoneOffset>, ResultSetIterator.Converter<ZoneOffset> {
    public static final ZoneOffsetJdbcAdapter ADAPTER = new ZoneOffsetJdbcAdapter();

    public @NotNull ZoneOffset createInstance(int totalSeconds) {
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    @Override
    public @NotNull Integer toValueObject(@NotNull ZoneOffset instance) {
        return instance.getTotalSeconds();
    }

    @Override
    public @NotNull ZoneOffset apply(@NotNull ResultSet resultSet) throws SQLException {
        return createInstance(resultSet.getInt(1));
    }
}
