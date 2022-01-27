package io.webby.orm.api.entity;

import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ColumnSet {
    @NotNull Collection<? extends Column> columns();
}
