package io.webby.orm.api.query;

import java.util.List;

public class Offset extends Unit implements Clause, Representable {
    private final int offset;

    public Offset(int offset) {
        super("OFFSET ?", List.of(offset));
        this.offset = offset;
    }

    public static Offset of(int offset) {
        return new Offset(offset);
    }

    public int offsetValue() {
        return offset;
    }
}
