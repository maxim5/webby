package io.webby.demo.model;

import io.spbx.orm.api.annotate.Sql.Default;

public record LongsModel(@Default("0") long foo, @Default("0") long bar, @Default("0") long value) {
}
