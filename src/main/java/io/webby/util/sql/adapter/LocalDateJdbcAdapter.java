package io.webby.util.sql.adapter;

import java.time.LocalDate;

public class LocalDateJdbcAdapter {
    public static LocalDate createInstance(java.sql.Date value) {
        return value.toLocalDate();
    }

    public static void fillArrayValues(LocalDate instance, Object[] array, int start) {
        array[start] = java.sql.Date.valueOf(instance);
    }
}
