package io.webby.demo.model;

import io.webby.orm.api.annotate.Sql;

public record LongsModel(@Sql(defaults = "0") long foo,
                         @Sql(defaults = "0") long bar,
                         @Sql(defaults = "0") long value) {
}
