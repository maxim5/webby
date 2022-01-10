package io.webby.url.convert;

import org.jetbrains.annotations.Nullable;

import static io.webby.url.convert.ConversionError.failIf;

public abstract class LengthConstraint {
    protected final int maxLength;

    public LengthConstraint(int maxLength) {
        this.maxLength = maxLength;
    }

    public void validateString(@Nullable String name, @Nullable CharSequence value) {
        failIf(value == null, name, "Variable is expected, but not provided");
        failIf(value.length() > maxLength, name, "The value exceeds max length %d", maxLength);
    }

    @Override
    public String toString() {
        return "%s[%d]".formatted(getClass().getSimpleName(), maxLength);
    }
}
