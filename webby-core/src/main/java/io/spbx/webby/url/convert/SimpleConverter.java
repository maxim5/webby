package io.spbx.webby.url.convert;

import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.spbx.webby.url.convert.ConversionError.failIf;

public interface SimpleConverter<T> extends Constraint<T> {
    T convert(@NotNull String value) throws ConversionError;

    @Override
    default T apply(@Nullable CharArray value) throws ConversionError {
        failIf(value == null, null, "Variable is expected, but not provided");
        return convert(value.toString());
    }
}
