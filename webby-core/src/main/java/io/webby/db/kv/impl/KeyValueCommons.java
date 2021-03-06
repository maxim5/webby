package io.webby.db.kv.impl;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

public class KeyValueCommons {
    @SuppressWarnings("UnstableApiUsage")
    public static <T> @NotNull Stream<T> streamOf(@NotNull Iterator<T> iterator) {
        return Streams.stream(iterator);
    }
}
