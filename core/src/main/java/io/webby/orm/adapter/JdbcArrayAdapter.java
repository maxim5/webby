package io.webby.orm.adapter;

import org.jetbrains.annotations.NotNull;

public interface JdbcArrayAdapter<E> {
    void fillArrayValues(E instance, @NotNull Object[] array, int start);

    @NotNull Object[] toNewValuesArray(E instance);
}
