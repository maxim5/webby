package io.spbx.webby.url.convert;

import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.Nullable;

public interface Validator extends Constraint<CharArray> {
    void validate(@Nullable CharArray value) throws ConversionError;

    @Override
    default CharArray apply(@Nullable CharArray value) throws ConversionError {
        validate(value);
        return value;
    }
}
