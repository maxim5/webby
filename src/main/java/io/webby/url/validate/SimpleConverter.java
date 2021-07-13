package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import io.webby.url.caller.ValidationError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SimpleConverter<T> extends Converter<T> {
    T convert(@NotNull String value);

    @Override
    default T convert(@Nullable CharBuffer value) {
        ValidationError.failIf(value == null, "Variable is expected, but not provided");
        return convert(value.toString());
    }
}
