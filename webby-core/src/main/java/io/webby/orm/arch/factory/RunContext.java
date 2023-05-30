package io.webby.orm.arch.factory;

import io.webby.orm.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

class RunContext implements AutoCloseable {
    private final AtomicBoolean closed = new AtomicBoolean();
    private final RunInputs inputs;
    private final ModelAdaptersScanner adaptersScanner;
    private final PojoArchCollector pojos;
    private final TableArchCollector tables;
    private final ErrorHandler errorHandler;

    public RunContext(@NotNull RunInputs inputs, @NotNull ModelAdaptersScanner adaptersScanner) {
        this.inputs = inputs;
        this.adaptersScanner = adaptersScanner;
        this.pojos = new PojoArchCollector();
        this.tables = new TableArchCollector();
        this.errorHandler = new ErrorHandler();
    }

    public @NotNull RunInputs inputs() {
        assert !closed.get() : "RunContext is closed";
        return inputs;
    }

    public @NotNull ModelAdaptersScanner adaptersScanner() {
        assert !closed.get() : "RunContext is closed";
        return adaptersScanner;
    }

    public @NotNull PojoArchCollector pojos() {
        assert !closed.get() : "RunContext is closed";
        return pojos;
    }

    public @NotNull TableArchCollector tables() {
        assert !closed.get() : "RunContext is closed";
        return tables;
    }

    public @NotNull ErrorHandler errorHandler() {
        return errorHandler;
    }

    @Override
    public void close() {
        closed.set(true);
    }
}
