package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcMultiValueAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

public class OffsetTimeJdbcAdapter implements JdbcMultiValueAdapter<OffsetTime> {
    public static final OffsetTimeJdbcAdapter ADAPTER = new OffsetTimeJdbcAdapter();

    @Override
    public int valuesNumber() {
        return 2;
    }

    public OffsetTime createInstance(Time time, int zoneOffsetSeconds) {
        LocalTime localTime = LocalTimeJdbcAdapter.ADAPTER.createInstance(time);
        ZoneOffset zoneOffset = ZoneOffsetJdbcAdapter.ADAPTER.createInstance(zoneOffsetSeconds);
        return OffsetTime.of(localTime, zoneOffset);
    }

    @Override
    public void fillArrayValues(OffsetTime instance, @NotNull Object[] array, int start) {
        array[start] = LocalTimeJdbcAdapter.ADAPTER.toValueObject(instance.toLocalTime());
        array[start+1] = ZoneOffsetJdbcAdapter.ADAPTER.toValueObject(instance.getOffset());
    }
}
