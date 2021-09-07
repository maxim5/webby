package io.webby.url.convert;

import io.routekit.util.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringConverter implements Converter<String> {
    public static final StringConverter UNLIMITED = new StringConverter(Integer.MAX_VALUE);
    public static final StringConverter MAX_256 = new StringConverter(256);

    private final int maxLength;

    public StringConverter(int maxLength) {
        this.maxLength = maxLength;
    }

    public void validateString(@Nullable String name, @Nullable CharSequence value) {
        ConversionError.failIf(value == null, name, "Variable is expected, but not provided");
        ConversionError.failIf(value.length() > maxLength, name, "The value exceeds max length %d".formatted(maxLength));
    }

    @Override
    public String convert(@NotNull String value) throws ConversionError {
        validateString(null, value);
        return value;
    }

    @Override
    public String apply(@Nullable CharArray value) throws ConversionError {
        validateString(null, value);
        return Objects.toString(value);
    }

    @Override
    public String toString() {
        return "StringConverter[%d]".formatted(maxLength);
    }
}
