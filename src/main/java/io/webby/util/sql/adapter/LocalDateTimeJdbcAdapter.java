package io.webby.util.sql.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeJdbcAdapter {
    public static LocalDateTime createInstance(Timestamp value) {
        return value.toLocalDateTime();
    }

    public static void fillArrayValues(LocalDateTime instance, Object[] array, int start) {
        array[start] = Timestamp.valueOf(instance);
    }
}
