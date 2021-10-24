package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QueryException extends RuntimeException {
    private final String query;
    private final List<Object> args;

    public QueryException(@NotNull String message, @NotNull String query, @Nullable Throwable cause) {
        this(message, query, List.of(), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, Object arg, @Nullable Throwable cause) {
        this(message, query, List.of(arg), cause);
    }

    public QueryException(@NotNull String message, @NotNull String query, @NotNull List<Object> args, @Nullable Throwable cause) {
        super(format(message, query, args), cause);
        this.query = query;
        this.args = args;
    }

    public String getQuery() {
        return query;
    }

    public List<Object> getArgs() {
        return args;
    }

    private static @NotNull String format(String message, String query, List<Object> args) {
        return "%s. Query: %s. Args: %s".formatted(message, query, args);
    }
}
