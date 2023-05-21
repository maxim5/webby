package io.webby.db.sql.tx;

import com.google.inject.Inject;
import io.webby.orm.api.Connector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TxRunnerImpl implements TxRunner {
    private final Connector connector;

    @Inject
    public TxRunnerImpl(@NotNull Connector connector) {
        this.connector = connector;
    }

    @Override
    public void run(@NotNull Runnable action) {
        connector.runner().tx().run(action::run);
    }

    @Override
    public <T> T run(@NotNull Supplier<T> action) {
        return connector.runner().tx().run(action::get);
    }
}
