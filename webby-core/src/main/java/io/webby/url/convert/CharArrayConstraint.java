package io.webby.url.convert;

import io.routekit.util.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharArrayConstraint extends LengthConstraint implements Constraint<CharArray> {
    public static final CharArrayConstraint UNLIMITED = new CharArrayConstraint(Integer.MAX_VALUE);
    public static final CharArrayConstraint MAX_256 = new CharArrayConstraint(256);

    public CharArrayConstraint(int maxLength) {
        super(maxLength);
    }

    @Override
    public CharArray apply(@Nullable CharArray value) throws ConversionError {
        validateString(null, value);
        return value;
    }

    @Override
    public CharArray applyWithName(@NotNull String name, @Nullable CharArray value) throws ConversionError {
        validateString(name, value);
        return value;
    }
}
