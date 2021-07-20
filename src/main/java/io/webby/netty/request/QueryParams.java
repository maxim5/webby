package io.webby.netty.request;

import io.routekit.util.CharBuffer;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.ConversionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class QueryParams {
    private final String path;
    private final String rawQuery;
    private final Map<String, List<String>> parameters;
    private final Map<String, Constraint<?>> validators;

    public QueryParams(@NotNull String path,
                       @NotNull String rawQuery,
                       @NotNull Map<String, List<String>> parameters,
                       @NotNull Map<String, Constraint<?>> validators) {
        this.path = path;
        this.rawQuery = rawQuery;
        this.parameters = parameters;
        this.validators = validators;
    }

    @NotNull
    public String path() {
        return path;
    }

    @NotNull
    public String rawQuery() {
        return rawQuery;
    }

    @NotNull
    public Map<String, List<String>> getAllParameters() {
        return parameters;
    }

    @Nullable
    public String getRawParameter(@NotNull String name) {
        List<String> values = parameters.get(name);
        return values != null ? values.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getValidatedParam(@NotNull String name) throws ConversionError {
        String value = getRawParameter(name);
        if (value == null) {
            return null;
        }

        Constraint<?> constraint = validators.get(name);
        return (T) constraint.applyWithName(name, new CharBuffer(value));
    }
}
