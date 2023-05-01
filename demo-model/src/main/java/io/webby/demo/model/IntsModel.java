package io.webby.demo.model;

import io.webby.orm.api.annotate.Sql.Default;

public record IntsModel(@Default("0") int foo, @Default("0") int bar, @Default("0") int value) {
}
