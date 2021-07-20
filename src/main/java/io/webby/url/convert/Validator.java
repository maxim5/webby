package io.webby.url.convert;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.Nullable;

public interface Validator extends Constraint<CharBuffer> {
    void validate(@Nullable CharBuffer value) throws ConversionError;

    @Override
    default CharBuffer apply(@Nullable CharBuffer value) throws ConversionError {
        validate(value);
        return value;
    }
}
