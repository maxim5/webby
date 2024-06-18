package io.spbx.webby.url.convert;

import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

import static io.spbx.webby.url.convert.ConversionError.assure;
import static io.spbx.webby.url.convert.ConversionError.failIf;

public class RegexValidator implements Validator {
    private final Pattern pattern;

    public RegexValidator(@NotNull Pattern pattern) {
        this.pattern = pattern;
    }

    public RegexValidator(@NotNull String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public void validate(@Nullable CharArray value) throws ConversionError {
        failIf(value == null, null, "Value is null");
        assure(pattern.matcher(value).matches(), null, "Value `%s` does not match the pattern: %s", value, pattern);
    }
}
