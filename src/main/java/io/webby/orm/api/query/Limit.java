package io.webby.orm.api.query;

import java.util.List;

public class Limit extends Unit implements Clause, Representable {
    private final int limit;

    public Limit(int limit) {
        super("LIMIT ?", List.of(limit));
        this.limit = limit;
    }

    public static Limit of(int limit) {
        return new Limit(limit);
    }

    public int limitValue() {
        return limit;
    }
}
