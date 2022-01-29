package io.webby.orm.api.query;

public class Offset extends Unit implements Filter, Representable {
    private final int offset;

    public Offset(int offset) {
        super("OFFSET ?", Args.of(offset));
        this.offset = offset;
    }

    public static Offset of(int offset) {
        return new Offset(offset);
    }

    public int offsetValue() {
        return offset;
    }
}
