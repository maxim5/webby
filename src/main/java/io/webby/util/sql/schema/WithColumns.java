package io.webby.util.sql.schema;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

public interface WithColumns {
    @NotNull Iterable<Column> columns();

    default int columnsNumber() {
        return (int) Streams.stream(columns()).count();
    }
}
