package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Limit extends Unit implements LimitClause {
    private final int value;

    public Limit(int value) {
        super("LIMIT ?", List.of(value));
        assert value > 0 : "Invalid limit value: " + value;
        this.value = value;
    }

    public static @NotNull Limit of(int value) {
        return new Limit(value);
    }

    public int limitValue() {
        return value;
    }
}
