package io.webby.util.sql.adapter;

@JdbcAdapt({char.class, Character.class})
public class CharacterJdbcAdapter {
    public static char createInstance(String value) {
        return value.charAt(0);
    }

    public static void fillArrayValues(char ch, Object[] array, int start) {
        array[start] = String.valueOf(ch);
    }
}
