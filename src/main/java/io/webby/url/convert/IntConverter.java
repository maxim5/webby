package io.webby.url.convert;

import io.routekit.util.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntConverter implements Converter<Integer> {
    public static IntConverter ANY = new IntConverter(Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static IntConverter POSITIVE = new IntConverter(1, Integer.MAX_VALUE);
    public static IntConverter NON_NEGATIVE = new IntConverter(0, Integer.MAX_VALUE);

    private final int min;
    private final int max;

    public IntConverter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int validateInt(@Nullable String name, @Nullable CharSequence value) {
        ConversionError.failIf(value == null, name, "Variable is expected, but not provided");
        try {
            int result = Integer.parseInt(value, 0, value.length(), 10);
            ConversionError.failIf(
                    result < min || result > max, name,
                    "Value `%d` is out of bounds: [%d, %d]".formatted(result, min, max));
            return result;
        } catch (NumberFormatException e) {
            throw new ConversionError(name, "Malformed integer: %s".formatted(value), e);
        }
    }

    @Override
    public Integer convert(@NotNull String value) throws ConversionError {
        return validateInt(null, value);
    }

    @Override
    public Integer apply(@Nullable CharArray value) throws ConversionError {
        return validateInt(null, value);
    }

    @Override
    public String toString() {
        return "IntConverter[%d, %d]".formatted(min, max);
    }
}
