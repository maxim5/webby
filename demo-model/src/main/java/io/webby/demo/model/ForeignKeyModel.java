package io.webby.demo.model;

import io.spbx.orm.api.Foreign;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.annotate.Model;
import io.spbx.orm.api.annotate.Sql;

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

    @Model(javaName = "ForeignKeyNullableModel")
    public record Nullable(int id,
                           @Sql.Null ForeignInt<InnerInt> innerInt,
                           @Sql.Null ForeignLong<InnerLong> innerLong,
                           @Sql.Null Foreign<String, InnerString> innerString) {}
}
