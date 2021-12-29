package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import org.jetbrains.annotations.NotNull;

@JdbcAdapt({char.class, Character.class})
public class CharacterJdbcAdapter {
    public static char createInstance(String value) {
        return value.charAt(0);
    }

    public static Object toValueObject(char ch) {
        return String.valueOf(ch);
    }

    public static void fillArrayValues(char ch, @NotNull Object[] array, int start) {
        array[start] = String.valueOf(ch);
    }

    public static @NotNull Object[] toNewValuesArray(char ch) {
        Object[] array = new Object[1];
        fillArrayValues(ch, array, 0);
        return array;
    }
}
