package io.spbx.util.props;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class InMemoryProperties implements MutablePropertyMap {
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    @Override
    public @Nullable String getOrNull(@NotNull String key) {
        return map.get(key);
    }

    @Override
    public @Nullable String setString(@NotNull String key, @NotNull String val) {
        return map.put(key, val);
    }
}
