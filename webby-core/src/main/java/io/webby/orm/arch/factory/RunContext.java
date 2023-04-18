package io.webby.orm.arch.factory;

import io.webby.orm.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

class RunContext {
    private final RunInputs inputs;
    private final ModelAdaptersScanner adaptersScanner;
    private final PojoArchCollector pojos;
    private final TableArchCollector tables;

    public RunContext(@NotNull RunInputs inputs,
                      @NotNull ModelAdaptersScanner adaptersScanner) {
        this.inputs = inputs;
        this.adaptersScanner = adaptersScanner;
        this.pojos = new PojoArchCollector();
        this.tables = new TableArchCollector();
    }

    public @NotNull RunInputs inputs() {
        return inputs;
    }

    public @NotNull ModelAdaptersScanner adaptersScanner() {
        return adaptersScanner;
    }

    public @NotNull PojoArchCollector pojos() {
        return pojos;
    }

    public @NotNull TableArchCollector tables() {
        return tables;
    }
}
