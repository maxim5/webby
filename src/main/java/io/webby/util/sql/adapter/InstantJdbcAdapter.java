package io.webby.util.sql.adapter;

import java.time.Instant;

public class InstantJdbcAdapter {
    // long!
    public static Instant createInstance(int value) {
        return Instant.ofEpochSecond(value);
    }

    public static void fillArrayValues(Instant instance, Object[] array, int start) {
        array[start] = instance.getEpochSecond();
    }
}
