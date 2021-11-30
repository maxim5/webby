package io.webby.orm.api.query;

public class Offset extends SimpleRepr implements Clause, Repr {
    private final int offset;

    public Offset(int offset) {
        super("OFFSET %d".formatted(offset));
        this.offset = offset;
    }

    public static Offset of(int offset) {
        return new Offset(offset);
    }

    public int offsetValue() {
        return offset;
    }
}
