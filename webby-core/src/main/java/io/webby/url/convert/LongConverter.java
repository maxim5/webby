package io.webby.url.convert;

import io.routekit.util.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.url.convert.ConversionError.failIf;

public class LongConverter implements SimpleConverter<Long> {
    public static final LongConverter ANY = new LongConverter(Long.MIN_VALUE, Long.MAX_VALUE);
    public static final LongConverter POSITIVE = new LongConverter(1, Long.MAX_VALUE);
    public static final LongConverter NON_NEGATIVE = new LongConverter(0, Long.MAX_VALUE);

    private final long min;
    private final long max;
    private final int radix;

    public LongConverter(long min, long max) {
        this(min, max, 10);
    }

    public LongConverter(long min, long max, int radix) {
        assert min <= max : "Invalid converter range: [%d, %d]".formatted(min, max);
        this.min = min;
        this.max = max;
        this.radix = radix;
    }

    public long validateLong(@Nullable String name, @Nullable CharSequence value) {
        failIf(value == null, name, "Variable is expected, but not provided");
        try {
            long result = Long.parseLong(value, 0, value.length(), radix);
            failIf(result < min || result > max, name, "Value `%d` is out of bounds: [%d, %d]", result, min, max);
            return result;
        } catch (NumberFormatException e) {
            throw new ConversionError(name, "Malformed integer: `%s`".formatted(value), e);
        }
    }

    @Override
    public Long convert(@NotNull String value) throws ConversionError {
        return validateLong(null, value);
    }

    @Override
    public Long apply(@Nullable CharArray value) throws ConversionError {
        return validateLong(null, value);
    }

    @Override
    public String toString() {
        return "LongConverter[%d, %d, %d]".formatted(min, max, radix);
    }
}
