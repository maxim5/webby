package io.webby.util.sql.arch;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithColumns {
    @NotNull List<Column> columns();

    default int columnsNumber() {
        return columns().size();
    }
}
