package io.webby.db.sql.tx;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface TxRunner {
    void run(@NotNull Runnable action);

    <T> T run(@NotNull Supplier<T> action);
}
