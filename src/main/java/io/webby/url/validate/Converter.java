package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

public interface Converter<T> extends Validator {
    T convert(@NotNull CharBuffer value);
}
