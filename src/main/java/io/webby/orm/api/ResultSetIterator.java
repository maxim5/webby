package io.webby.orm.api;

import com.google.errorprone.annotations.MustBeClosed;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResultSetIterator<E> implements Iterator<E>, Closeable {
    private final ResultSet resultSet;
    private final boolean ownsStatement;
    private final Converter<E> converter;
    private final AtomicBoolean nextCalled = new AtomicBoolean();
    private final AtomicBoolean hasNextCached = new AtomicBoolean();

    public ResultSetIterator(@NotNull ResultSet resultSet, boolean ownsStatement, @NotNull Converter<E> converter) {
        this.resultSet = resultSet;
        this.ownsStatement = ownsStatement;
        this.converter = converter;
    }

    @MustBeClosed
    public static <E> @NotNull ResultSetIterator<E> of(@NotNull ResultSet resultSet, @NotNull Converter<E> converter) {
        return new ResultSetIterator<>(resultSet, true, converter);
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
            // Calling the method close on a ResultSet object that is already closed is a no-op.
            // Calling the method close on a Statement object that is already closed has no effect.
            Statement statement = ownsStatement ? getStatementOrNull() : null;
            if (statement != null) {
                statement.close();
            }
            resultSet.close();
        } catch (SQLException e) {
            Rethrow.rethrow(e);
        }
    }

    private @Nullable Statement getStatementOrNull() {
        try {
            return resultSet.getStatement();
        } catch (SQLException e) {
            return null;
        }
    }

    public interface Converter<E> {
        E convert(@NotNull ResultSet resultSet) throws SQLException;
    }
}
