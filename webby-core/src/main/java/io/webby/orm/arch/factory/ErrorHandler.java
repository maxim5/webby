package io.webby.orm.arch.factory;

import io.webby.orm.arch.InvalidSqlModelException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

class ErrorHandler {
    private final AtomicReference<Field> currentField = new AtomicReference<>();

    public void setCurrentField(@NotNull Field field) {
        currentField.set(field);
    }

    public @NotNull RuntimeException handleRuntimeException(@NotNull RuntimeException e) {
        Field field = currentField.get();
        if (field == null) {
            return e;
        }

        String message = "Error while processing the model: `%s.%s`"
            .formatted(field.getDeclaringClass().getSimpleName(), field.getName());
        return new InvalidSqlModelException(message, e);
    }
}
