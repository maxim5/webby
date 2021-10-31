package io.webby.util.sql.adapter;

import java.sql.Timestamp;
import java.time.Instant;

public class InstantJdbcAdapter {
    public static Instant createInstance(Timestamp value) {
        return value.toInstant();
    }

    public static void fillArrayValues(Instant instance, Object[] array, int start) {
        array[start] = Timestamp.from(instance);
    }
}
