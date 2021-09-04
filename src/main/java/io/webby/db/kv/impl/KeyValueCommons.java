package io.webby.db.kv.impl;

import com.google.common.collect.Streams;
import io.webby.util.func.ThrowRunnable;
import io.webby.util.func.ThrowSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

import static io.webby.util.Rethrow.rethrow;

public class KeyValueCommons {
    // move to Rethrow?
    public static <E extends Throwable> void quiet(@NotNull ThrowRunnable<E> action) {
        try {
            action.run();
        } catch (Throwable e) {
            rethrow(e);
        }
    }

    // move to Rethrow?
    public static <T, E extends Throwable> T quiet(@NotNull ThrowSupplier<T, E> action) {
        try {
            return action.get();
        } catch (Throwable e) {
            return rethrow(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> @NotNull Stream<T> streamOf(@NotNull Iterator<T> iterator) {
        return Streams.stream(iterator);
    }
}
