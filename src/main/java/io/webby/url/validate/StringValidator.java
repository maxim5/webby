package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import io.webby.url.caller.ValidationError;
import org.jetbrains.annotations.NotNull;

public class StringValidator implements Validator {
    public static final StringValidator UNLIMITED = new StringValidator(Integer.MAX_VALUE);
    public static final StringValidator DEFAULT_256 = new StringValidator(256);

    private final int maxLength;

    public StringValidator(int maxLength) {
        this.maxLength = maxLength;
    }

    public void validateString(@NotNull String name, @NotNull CharSequence value) {
        ValidationError.failIf(value.length() > maxLength, "`%s` value exceeds max length %d".formatted(name, maxLength));
    }

    @Override
    public String toString() {
        return "StringValidator[%d]".formatted(maxLength);
    }
}
