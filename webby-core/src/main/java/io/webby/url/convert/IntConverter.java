package io.webby.url.convert;

import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.url.convert.ConversionError.failIf;

public class IntConverter implements SimpleConverter<Integer> {
    public static final IntConverter ANY = new IntConverter(Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntConverter POSITIVE = new IntConverter(1, Integer.MAX_VALUE);
    public static final IntConverter NON_NEGATIVE = new IntConverter(0, Integer.MAX_VALUE);

    private final int min;
    private final int max;
    private final int radix;

    public IntConverter(int min, int max) {
        this(min, max, 10);
    }

    public IntConverter(int min, int max, int radix) {
        assert min <= max : "Invalid converter range: [%d, %d]".formatted(min, max);
        this.min = min;
        this.max = max;
        this.radix = radix;
    }

    public int validateInt(@Nullable String name, @Nullable CharSequence value) {
        failIf(value == null, name, "Variable is expected, but not provided");
        try {
            int result = Integer.parseInt(value, 0, value.length(), radix);
            failIf(result < min || result > max, name, "Value `%d` is out of bounds: [%d, %d]", result, min, max);
            return result;
        } catch (NumberFormatException e) {
            throw new ConversionError(name, "Malformed integer: `%s`".formatted(value), e);
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
        return "IntConverter[%d, %d, %d]".formatted(min, max, radix);
    }
}
