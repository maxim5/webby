package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class LazyBoolean {
    private final BooleanSupplier supplier;
    private boolean computed;
    private boolean value;

    public LazyBoolean(@NotNull BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean get() {
        if (!computed) {
            value = supplier.getAsBoolean();
            computed = true;
        }
        return value;
    }
}
