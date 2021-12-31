package io.webby.perf.stats;

import com.google.mu.util.stream.BiStream;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Stat {
    DB_GET(1, Unit.CALLS),
    DB_SET(2, Unit.CALLS),
    DB_DELETE(3, Unit.CALLS),
    DB_SIZE(4, Unit.CALLS),
    DB_SCAN(5, Unit.CALLS),
    DB_IO(6, Unit.CALLS),

    CODEC_READ(101, Unit.BYTES),
    CODEC_WRITE(102, Unit.BYTES),

    RENDER(200, Unit.BYTES);

    public static final Map<Integer, Stat> VALUES = Arrays.stream(Stat.values()).collect(Collectors.toMap(Stat::key, s -> s));
    public static final Map<Integer, String> NAMES = BiStream.from(VALUES).mapValues(stat -> stat.name().toLowerCase()).toMap();
    public static final int MAX_NAME_LENGTH = NAMES.values().stream().mapToInt(String::length).max().orElseThrow();

    private final int key;
    private final Unit unit;

    Stat(int key, Unit unit) {
        this.key = key;
        this.unit = unit;
    }

    public int key() {
        return key;
    }

    public Unit unit() {
        return unit;
    }

    public String lowerName() {
        return NAMES.get(key);
    }

    public enum Unit {
        CALLS,
        BYTES;

        public String lowerName() {
            return name().toLowerCase();
        }
    }
}
