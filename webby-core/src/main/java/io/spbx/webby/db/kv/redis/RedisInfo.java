package io.spbx.webby.db.kv.redis;

import com.google.common.collect.ImmutableMap;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// See https://redis.io/commands/info
public class RedisInfo {
    private final ImmutableMap<String, String> props;

    public RedisInfo(@NotNull ImmutableMap<String, String> props) {
        this.props = props;
    }

    public static @NotNull RedisInfo parseFrom(@NotNull String info) {
        ImmutableMap<String, String> props = info.lines()
            .filter(line -> !line.isBlank() && !line.startsWith("#"))
            .map(line -> Pair.of(line.split(":", 2)))
            .collect(ImmutableMap.toImmutableMap(Pair::first, Pair::second));
        return new RedisInfo(props);
    }

    public @NotNull Map<String, String> props() {
        return props;
    }

    public @NotNull String version() {
        return props.getOrDefault("redis_version", "");
    }

    public boolean isVersionAfter(@NotNull String version) {
        return version().compareTo(version) >= 0;
    }
}
