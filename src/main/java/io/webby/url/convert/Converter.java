package io.webby.url.convert;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Converter<T> extends Constraint<T> {
    T convert(@NotNull String value) throws ConversionError;

    @Override
    default T apply(@Nullable CharBuffer value) throws ConversionError {
        ConversionError.failIf(value == null, null, "Variable is expected, but not provided");
        return convert(value.toString());
    }
}
