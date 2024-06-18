package io.spbx.webby.db.kv.oak;

import com.yahoo.oak.OakComparator;
import com.yahoo.oak.OakSerializer;
import org.jetbrains.annotations.NotNull;

public record OakRecord<T>(@NotNull OakComparator<T> comparator,
                           @NotNull OakSerializer<T> serializer,
                           @NotNull T minValue) {
}
