package io.webby.perf.stats;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Stat {
    DB_GET(1),
    DB_SET(2),
    DB_DELETE(3),
    DB_SIZE(4),
    DB_SCAN(5),
    DB_IO(6),

    CODEC_READ(101),
    CODEC_WRITE(102),

    RENDER(200);

    public static final Map<Integer, Stat> VALUES = Arrays.stream(Stat.values()).collect(Collectors.toMap(Stat::key, s -> s));

    private final int key;

    Stat(int key) {
        this.key = key;
    }

    public int key() {
        return key;
    }
}
