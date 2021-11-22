package io.webby.examples.model;

import io.webby.orm.api.Foreign;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;

public record ForeignKeyModel(long id,
                              ForeignInt<InnerInt> innerInt,
                              ForeignLong<InnerLong> innerLong,
                              Foreign<String, InnerString> innerString) {

    public record InnerInt(int id, int value) {}
    public record InnerLong(long id, long value) {}
    public record InnerString(String id, String value) {}
}
