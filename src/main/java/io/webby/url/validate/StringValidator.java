package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import io.webby.url.caller.ValidationError;
import org.jetbrains.annotations.NotNull;

public class StringValidator {
    public static final StringValidator UNLIMITED = new StringValidator(Integer.MAX_VALUE);
    public static final StringValidator DEFAULT_256 = new StringValidator(256);

    private final int maxLength;

    public StringValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    public void validateString(@NotNull String name, @NotNull CharBuffer value) {
        ValidationError.failIf(value.length() > maxLength, "%s value is too long: max=%d".formatted(name, maxLength));
    }
}
