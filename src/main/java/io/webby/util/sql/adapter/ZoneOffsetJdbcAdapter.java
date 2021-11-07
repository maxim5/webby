package io.webby.util.sql.adapter;

import java.time.ZoneOffset;

public class ZoneOffsetJdbcAdapter implements JdbcSingleValueAdapter<ZoneOffset> {
    public static final ZoneOffsetJdbcAdapter ADAPTER = new ZoneOffsetJdbcAdapter();

    public ZoneOffset createInstance(int totalSeconds) {
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    @Override
    public Object toValueObject(ZoneOffset instance) {
        return instance.getTotalSeconds();
    }
}
