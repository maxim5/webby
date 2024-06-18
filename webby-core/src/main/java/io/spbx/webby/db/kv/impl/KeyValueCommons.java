package io.spbx.webby.db.kv.impl;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

public class KeyValueCommons {
    public static <T> @NotNull Stream<T> streamOf(@NotNull Iterator<T> iterator) {
        return Streams.stream(iterator);
    }
}
