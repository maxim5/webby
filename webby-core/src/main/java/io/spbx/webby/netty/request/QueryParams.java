package io.spbx.webby.netty.request;

import com.google.common.collect.ImmutableMap;
import com.google.mu.util.stream.BiStream;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.spbx.util.base.CharArray;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.url.convert.Constraint;
import io.spbx.webby.url.convert.ConversionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.spbx.util.base.EasyCast.castAny;
import static java.util.Objects.requireNonNull;

public class QueryParams implements PropertyMap {
    private final String path;
    private final String query;
    private final ImmutableMap<String, List<String>> parameters;
    private final ImmutableMap<String, Constraint<?>> constraints;

    public QueryParams(@NotNull String path,
                       @NotNull String query,
                       @NotNull Map<String, List<String>> parameters,
                       @NotNull Map<String, Constraint<?>> constraints) {
        this.path = path;
        this.query = query;
        this.parameters = ImmutableMap.copyOf(parameters);
        this.constraints = ImmutableMap.copyOf(constraints);
    }

    public static @NotNull QueryParams fromDecoder(@NotNull QueryStringDecoder decoder,
                                                   @NotNull Map<String, Constraint<?>> constraints) {
        return new QueryParams(decoder.path(), decoder.rawQuery(), decoder.parameters(), constraints);
    }

    public @NotNull String path() {
        return path;
    }

    public @NotNull String query() {
        return query;
    }

    public int size() {
        return parameters.size();
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    public @NotNull Map<String, List<String>> getMap() {
        return parameters;
    }

    public @NotNull Set<String> keys() {
        return parameters.keySet();
    }

    public @NotNull Stream<Map.Entry<String, List<String>>> stream() {
        return parameters.entrySet().stream();
    }

    public @NotNull BiStream<String, List<String>> biStream() {
        return BiStream.from(parameters);
    }

    public boolean contains(@NotNull String name) {
        return parameters.containsKey(name);
    }

    public @NotNull List<String> getAll(@NotNull String name) {
        return requireNonNull(parameters.getOrDefault(name, List.of()));
    }

    public @Nullable String getOrNull(@NotNull String name) {
        List<String> values = parameters.get(name);
        return values != null ? values.getFirst() : null;
    }

    public <T> @Nullable T getConvertedIfExists(@NotNull String name) throws ConversionError {
        String value = getOrNull(name);
        if (value == null) {
            return null;
        }
        Constraint<?> constraint = requireNonNull(constraints.get(name));
        return castAny(constraint.applyWithName(name, new CharArray(value)));
    }

    public <T> @NotNull T getConvertedOrDie(@NotNull String name) throws ConversionError {
        String value = requireNonNull(getOrNull(name), () -> "Param `%s` is not query: %s".formatted(name, keys()));
        Constraint<?> constraint = requireNonNull(constraints.get(name));
        return castAny(constraint.applyWithName(name, new CharArray(value)));
    }
}
