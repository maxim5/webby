package io.webby.demo.model;

import io.webby.orm.api.annotate.Sql;

public record IntsModel(@Sql(defaults = "0") int foo,
                        @Sql(defaults = "0") int bar,
                        @Sql(defaults = "0") int value) {
}
