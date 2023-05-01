package io.webby.orm.adapter;

import org.jetbrains.annotations.NotNull;

/**
 * An adapter which requires multiple JDBC values to represent the entity.
 *
 * @param <E> the entity type
 */
public interface JdbcMultiValueAdapter<E> extends JdbcArrayAdapter<E> {
    /**
     * Returns the number of values necessary to represent the entity.
     */
    int valuesNumber();

    @Override
    default @NotNull Object[] toNewValuesArray(E instance) {
        Object[] array = new Object[valuesNumber()];
        fillArrayValues(instance, array, 0);
        return array;
    }
}
