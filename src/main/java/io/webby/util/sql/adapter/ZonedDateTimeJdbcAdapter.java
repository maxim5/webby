package io.webby.util.sql.adapter;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeJdbcAdapter {
    public static ZonedDateTime createInstance(Timestamp value) {
        return value.toLocalDateTime().atZone(ZoneId.systemDefault());
    }

    public static void fillArrayValues(ZonedDateTime instance, Object[] array, int start) {
        array[start] = Timestamp.valueOf(instance.toLocalDateTime());
    }
}
