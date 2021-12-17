package io.webby.examples.model;

import io.webby.orm.api.Foreign;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.annotate.Model;

public record ForeignKeyModel(long id,
                              ForeignInt<InnerInt> innerInt,
                              ForeignLong<InnerLong> innerLong,
                              Foreign<String, InnerString> innerString) {

    @Model(javaName = "FKInt")
    public record InnerInt(int id, int value) {}
    @Model(javaName = "FKLong")
    public record InnerLong(long id, long value) {}
    @Model(javaName = "FKString")
    public record InnerString(String id, String value) {}
}
