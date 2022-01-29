package io.webby.orm.arch;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HasColumns {
    @NotNull List<Column> columns();

    default int columnsNumber() {
        return columns().size();
    }
}
