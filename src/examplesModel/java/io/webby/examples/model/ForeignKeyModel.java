package io.webby.examples.model;

import io.webby.util.sql.api.Foreign;
import io.webby.util.sql.api.ForeignInt;
import io.webby.util.sql.api.ForeignLong;

public record ForeignKeyModel(long id,
                              ForeignInt<InnerInt> innerInt,
                              ForeignLong<InnerLong> innerLong,
                              Foreign<String, InnerString> innerString) {

    public record InnerInt(int id, int value) {}
    public record InnerLong(long id, long value) {}
    public record InnerString(String id, String value) {}
}
