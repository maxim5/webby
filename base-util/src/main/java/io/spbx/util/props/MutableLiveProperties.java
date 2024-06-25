package io.spbx.util.props;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ThreadSafe
public class MutableLiveProperties extends LiveProperties implements MutablePropertyMap {
    private final ConcurrentMap<String, String> overwrites = new ConcurrentHashMap<>();

    protected MutableLiveProperties(@NotNull Path configPath) {
        super(configPath);
    }

    public static @NotNull MutableLiveProperties running(@NotNull Path configPath) {
        MutableLiveProperties liveConfig = new MutableLiveProperties(configPath);
        liveConfig.updatePropertiesMap();
        liveConfig.startMonitorThread();
        return liveConfig;
    }

    public static @NotNull MutableLiveProperties idle(@NotNull Path configPath) {
        MutableLiveProperties liveConfig = new MutableLiveProperties(configPath);
        liveConfig.updatePropertiesMap();
        return liveConfig;
    }

    @Override
    public @Nullable String getOrNull(@NotNull String key) {
        String value = overwrites.get(key);
        return value != null ? value : super.getOrNull(key);
    }

    @Override
    public @Nullable String setString(@NotNull String key, @NotNull String value) {
        return overwrites.put(key, value);
    }
}
