package io.webby.util.sql.adapter;

import org.jetbrains.annotations.NotNull;

public interface JdbcSingleColumnArrayAdapter<E> extends JdbcArrayAdapter<E> {
    Object toValueObject(E instance);

    @Override
    default void fillArrayValues(E instance, @NotNull Object[] array, int start) {
        array[start] = toValueObject(instance);
    }

    @Override
    default @NotNull Object[] toNewValuesArray(E instance) {
        Object[] array = new Object[1];
        fillArrayValues(instance, array, 0);
        return array;
    }
}
