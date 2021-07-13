package io.webby.url.validate;

import io.routekit.util.CharBuffer;
import io.webby.url.caller.ValidationError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntValidator implements Validator, Converter<Integer> {
    public static IntValidator ANY = new IntValidator(Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static IntValidator POSITIVE = new IntValidator(1, Integer.MAX_VALUE);
    public static IntValidator NON_NEGATIVE = new IntValidator(0, Integer.MAX_VALUE);

    private final int min;
    private final int max;

    public IntValidator(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int validateInt(@NotNull String name, @Nullable CharSequence value) {
        ValidationError.failIf(value == null, "Variable `%s` is expected, but not provided".formatted(name));
        try {
            int result = Integer.parseInt(value, 0, value.length(), 10);
            ValidationError.failIf(
                    result < min || result > max,
                    "`%s` value `%d` is out of bounds: [%d, %d]".formatted(name, result, min, max));
            return result;
        } catch (NumberFormatException e) {
            throw new ValidationError("Malformed `%s` integer: %s".formatted(name, value), e);
        }
    }

    @Override
    public Integer convert(@Nullable CharBuffer value) {
        return validateInt("%integer%", value);
    }

    @NotNull
    public SimpleConverter<Integer> asSimpleConverter() {
        return value -> validateInt("%integer%", value);
    }

    @Override
    public String toString() {
        return "IntValidator[%d, %d]".formatted(min, max);
    }
}
