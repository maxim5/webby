package io.webby.orm.api;

import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResultSetIterator<E> implements Iterator<E>, Closeable {
    private final ResultSet resultSet;
    private final Converter<E> converter;
    private final AtomicBoolean nextCalled = new AtomicBoolean();
    private final AtomicBoolean hasNextCached = new AtomicBoolean();

    public ResultSetIterator(@NotNull ResultSet resultSet, @NotNull Converter<E> converter) {
        this.resultSet = resultSet;
        this.converter = converter;
    }

    @Override
    public boolean hasNext() {
        // switch to resultSet.isLast() when it's supported:
        // https://github.com/xerial/sqlite-jdbc/issues/682
        try {
            if (nextCalled.compareAndSet(false, true)) {
                hasNextCached.set(resultSet.next());
            }
            return hasNextCached.get();
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public E next() {
        try {
            nextCalled.set(false);
            return converter.convert(resultSet);
        } catch (SQLException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public void remove() {
        try {
            resultSet.deleteRow();
        } catch (SQLException e) {
            Rethrow.rethrow(e);
        }
    }

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            Rethrow.rethrow(e);
        }
    }

    public interface Converter<E> {
        E convert(ResultSet resultSet) throws SQLException;
    }
}
