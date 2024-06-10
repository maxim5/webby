package io.webby.url.convert;

import io.webby.util.base.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface Constraint<T> extends Function<CharArray, T> {
    @Override
    T apply(@Nullable CharArray value) throws ConversionError;

    default T applyWithName(@NotNull String name, @Nullable CharArray value) throws ConversionError {
        try {
            return apply(value);
        } catch (ConversionError e) {
            if (!e.hasVariable()) {
                throw new ConversionError(name, e.getMessage(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            throw new ConversionError(name, "Failed to validate `%s`: %s".formatted(name, e.getMessage()), e);
        }
    }
}
