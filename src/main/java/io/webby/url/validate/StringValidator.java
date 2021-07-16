package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringValidator implements Validator, Converter<String> {
    public static final StringValidator UNLIMITED = new StringValidator(Integer.MAX_VALUE);
    public static final StringValidator DEFAULT_256 = new StringValidator(256);

    private final int maxLength;

    public StringValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    public void validateString(@NotNull String name, @Nullable CharSequence value) {
        ValidationError.failIf(value == null, "Variable `%s` is expected, but not provided".formatted(name));
        ValidationError.failIf(value.length() > maxLength, "`%s` value exceeds max length %d".formatted(name, maxLength));
    }

    @Override
    public String convert(@Nullable CharBuffer value) {
        validateString("%string%", value);
        return Objects.toString(value);
    }

    @NotNull
    public SimpleConverter<String> asSimpleConverter() {
        return value -> {
            validateString("%string%", value);
            return value;
        };
    }

    @Override
    public String toString() {
        return "StringValidator[%d]".formatted(maxLength);
    }
}
