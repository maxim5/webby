package io.webby.orm.adapter.time;

import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcMultiValueAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@JdbcAdapt(value = OffsetDateTime.class, names = {"timestamp", "zone_offset_seconds"})
public class OffsetDateTimeJdbcAdapter implements JdbcMultiValueAdapter<OffsetDateTime> {
    public static final OffsetDateTimeJdbcAdapter ADAPTER = new OffsetDateTimeJdbcAdapter();

    @Override
    public int valuesNumber() {
        return 2;
    }

    public OffsetDateTime createInstance(Timestamp timestamp, int zoneOffsetSeconds) {
        LocalDateTime localDateTime = LocalDateTimeJdbcAdapter.ADAPTER.createInstance(timestamp);
        ZoneOffset zoneOffset = ZoneOffsetJdbcAdapter.ADAPTER.createInstance(zoneOffsetSeconds);
        return OffsetDateTime.of(localDateTime, zoneOffset);
    }

    @Override
    public void fillArrayValues(OffsetDateTime instance, @NotNull Object[] array, int start) {
        array[start] = LocalDateTimeJdbcAdapter.ADAPTER.toValueObject(instance.toLocalDateTime());
        array[start+1] = ZoneOffsetJdbcAdapter.ADAPTER.toValueObject(instance.getOffset());
    }
}
