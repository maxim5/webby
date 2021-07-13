package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.Nullable;

public interface Converter<T> extends Validator {
    T convert(@Nullable CharBuffer value);
}
