package io.spbx.webby.db.sql.tx;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class NoTxRunner implements TxRunner {
    @Override
    public void run(@NotNull Runnable action) {
        action.run();
    }

    @Override
    public <T> T run(@NotNull Supplier<T> action) {
        return action.get();
    }
}
