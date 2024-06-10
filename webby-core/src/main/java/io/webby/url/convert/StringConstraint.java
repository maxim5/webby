package io.webby.url.convert;

import io.webby.util.base.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringConstraint extends LengthConstraint implements Constraint<String> {
    public static final StringConstraint UNLIMITED = new StringConstraint(Integer.MAX_VALUE);
    public static final StringConstraint MAX_256 = new StringConstraint(256);

    public StringConstraint(int maxLength) {
        super(maxLength);
    }

    @Override
    public String apply(@Nullable CharArray value) throws ConversionError {
        validateString(null, value);
        return String.valueOf(value);
    }

    @Override
    public String applyWithName(@NotNull String name, @Nullable CharArray value) throws ConversionError {
        validateString(name, value);
        return String.valueOf(value);
    }
}
