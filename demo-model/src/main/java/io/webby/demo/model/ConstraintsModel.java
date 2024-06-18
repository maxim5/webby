package io.webby.demo.model;

import io.spbx.orm.api.annotate.Sql;

public record ConstraintsModel(@Sql(primary = true, name = "key_id") int keyId,
                               @Sql(unique = true, defaults = "0") int fprint,
                               @Sql.Unique Range range,
                               @Sql.Name("name") String displayName,
                               @Sql.Null String s) {
    public record Range(int from, int to) {}
}
