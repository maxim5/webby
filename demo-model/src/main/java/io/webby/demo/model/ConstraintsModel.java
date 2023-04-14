package io.webby.demo.model;

import io.webby.orm.api.annotate.Sql;

public record ConstraintsModel(@Sql(primary = true) int keyId,
                               @Sql(unique = true) int fprint,
                               @Sql(unique = true) Range range,
                               @Sql("name") String displayName) {
    public record Range(int from, int to) {}
}
