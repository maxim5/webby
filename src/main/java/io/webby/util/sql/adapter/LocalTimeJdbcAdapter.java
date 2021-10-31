package io.webby.util.sql.adapter;

import java.sql.Time;
import java.time.LocalTime;

public class LocalTimeJdbcAdapter {
    public static LocalTime createInstance(Time value) {
        return value.toLocalTime();
    }

    public static void fillArrayValues(LocalTime instance, Object[] array, int start) {
        array[start] = Time.valueOf(instance);
    }
}
