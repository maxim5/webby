package io.webby.orm.adapter.chars;

import io.webby.orm.adapter.JdbcAdapt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JdbcAdapt({char.class, Character.class})
public class CharacterJdbcAdapter {
    public static char createInstance(@NotNull String value) {
        return value.charAt(0);
    }

    public static @NotNull String toValueObject(char ch) {
        return String.valueOf(ch);
    }

    public static void fillArrayValues(char ch, @Nullable Object @NotNull [] array, int start) {
        array[start] = String.valueOf(ch);
    }

    public static @Nullable Object @NotNull [] toNewValuesArray(char ch) {
        Object[] array = new Object[1];
        fillArrayValues(ch, array, 0);
        return array;
    }
}
