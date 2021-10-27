package io.webby.util.sql.adapter;

import java.time.Instant;

public class InstantJdbcAdapter {
    public static Instant createInstance(long value) {
        return Instant.ofEpochSecond(value);
    }

    public static void fillArrayValues(Instant instance, Object[] array, int start) {
        array[start] = instance.getEpochSecond();
    }
}
