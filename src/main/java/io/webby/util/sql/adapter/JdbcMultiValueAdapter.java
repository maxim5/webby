package io.webby.util.sql.adapter;

import org.jetbrains.annotations.NotNull;

public interface JdbcMultiValueAdapter <E> extends JdbcArrayAdapter<E> {
    int valuesNumber();

    @Override
    default @NotNull Object[] toNewValuesArray(E instance) {
        Object[] array = new Object[valuesNumber()];
        fillArrayValues(instance, array, 0);
        return array;
    }
}
