package io.webby.util.sql.adapter;

import java.sql.Date;
import java.time.LocalDate;

public class LocalDateJdbcAdapter {
    public static LocalDate createInstance(Date value) {
        return value.toLocalDate();
    }

    public static void fillArrayValues(LocalDate instance, Object[] array, int start) {
        array[start] = Date.valueOf(instance);
    }
}
