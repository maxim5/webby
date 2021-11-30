package io.webby.orm.api.query;

public class Limit extends SimpleRepr implements Clause, Repr {
    private final int limit;

    public Limit(int limit) {
        super("LIMIT %d".formatted(limit));
        this.limit = limit;
    }

    public static Limit of(int limit) {
        return new Limit(limit);
    }

    public int limitValue() {
        return limit;
    }
}
