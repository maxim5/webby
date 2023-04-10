package io.webby.orm.api;

import io.webby.orm.api.query.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An exception for errors during SQL query preparation, execution or result processing.
 */
public class QueryException extends RuntimeException {
    private final String query;
    private final List<?> args;

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Object @NotNull ... args) {
        this(message, query, Arrays.asList(args), null);
    }

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Throwable cause) {
        this(message, query, List.of(), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @NotNull Args arg, @Nullable Throwable cause) {
        this(message, query, arg.asList(), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Object arg, @Nullable Throwable cause) {
        this(message, query, Collections.singletonList(arg), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @NotNull List<?> args, @Nullable Throwable cause) {
        super(format(message, query, args), cause);
        this.query = query;
        this.args = args;
    }

    /**
     * Returns the query that caused the failure.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the list of arguments for the query.
     */
    public List<?> getArgs() {
        return args;
    }

    private static @NotNull String format(@NotNull String message, @NotNull String query, @NotNull List<?> args) {
        return "%s. Query:\n```\n%s\n```.\nArgs: `%s`".formatted(message, query, args);
    }
}
