package io.webby.netty.request;

import com.google.mu.util.stream.BiStream;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.routekit.util.CharBuffer;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.ConversionError;
import io.webby.url.view.RenderUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class QueryParams {
    private final String path;
    private final String query;
    private final Map<String, List<String>> parameters;  // TODO: make immutable
    private final Map<String, Constraint<?>> constraints;

    public QueryParams(@NotNull String path,
                       @NotNull String query,
                       @NotNull Map<String, List<String>> parameters,
                       @NotNull Map<String, Constraint<?>> constraints) {
        this.path = path;
        this.query = query;
        this.parameters = parameters;
        this.constraints = constraints;
    }

    @NotNull
    public static QueryParams fromDecoder(@NotNull QueryStringDecoder decoder,
                                          @NotNull Map<String, Constraint<?>> constraints) {
        return new QueryParams(decoder.path(), decoder.rawQuery(), decoder.parameters(), constraints);
    }

    @NotNull
    public String path() {
        return path;
    }

    @NotNull
    public String query() {
        return query;
    }

    public int size() {
        return parameters.size();
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @NotNull
    public Map<String, List<String>> getMap() {
        return parameters;
    }

    @NotNull
    public Set<String> keys() {
        return parameters.keySet();
    }

    @NotNull
    public Stream<Map.Entry<String, List<String>>> stream() {
        return parameters.entrySet().stream();
    }

    @NotNull
    public BiStream<String, List<String>> biStream() {
        return BiStream.from(parameters);
    }

    public boolean contains(@NotNull String name) {
        return parameters.containsKey(name);
    }

    @NotNull
    public List<String> getAll(@NotNull String name) {
        return parameters.getOrDefault(name, List.of());
    }

    @Nullable
    public String getOrNull(@NotNull String name) {
        List<String> values = parameters.get(name);
        return values != null ? values.get(0) : null;
    }

    @Nullable
    public <T> T getConvertedOrNull(@NotNull String name) throws ConversionError {
        String value = getOrNull(name);
        if (value == null) {
            return null;
        }

        Constraint<?> constraint = constraints.get(name);
        return RenderUtil.castAny(constraint.applyWithName(name, new CharBuffer(value)));
    }

    /*
    @Nullable
    public <T> T getConvertedOrNull(@NotNull String name, @NotNull Function<String, T> convert) throws ConversionError {
        String value = getOrNull(name);
        if (value == null) {
            return null;
        }

        Constraint<?> constraint = constraints.getOrDefault(name,
                chars -> chars != null ? convert.apply(chars.toString()) : null);
        return RenderUtil.castAny(constraint.applyWithName(name, new CharBuffer(value)));
    }
    */

    public int getInt(@NotNull String name, int def) {
        try {
            String value = getOrNull(name);
            return value != null ? Integer.parseInt(value) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    public long getLong(@NotNull String name, long def) {
        try {
            String parameter = getOrNull(name);
            return parameter != null ? Long.parseLong(parameter) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    public byte getByte(@NotNull String name, byte def) {
        try {
            String parameter = getOrNull(name);
            return parameter != null ? Byte.parseByte(parameter) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    public boolean getBool(@NotNull String name, boolean def) {
        String value = getOrNull(name);
        return value != null ? Boolean.parseBoolean(value) : def;
    }

    public boolean getBool(@NotNull String name) {
        return getBool(name, false);
    }

    public boolean getBoolOrTrue(@NotNull String name) {
        return getBool(name, true);
    }

    public float getFloat(@NotNull String name, float def) {
        try {
            String value = getOrNull(name);
            return value != null ? Float.parseFloat(value) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    public double getDouble(@NotNull String name, double def) {
        try {
            String value = getOrNull(name);
            return value != null ? Double.parseDouble(value) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }
}
