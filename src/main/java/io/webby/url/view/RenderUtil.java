package io.webby.url.view;

import com.google.common.io.Closeables;
import io.webby.util.Casting;
import io.webby.util.Rethrow;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;
import java.util.function.Function;

public class RenderUtil {
    private static final int DEFAULT_SIZE_BYTES = 1024;
    private static final int DEFAULT_SIZE_CHARS = 1024;

    @NotNull
    public static <E extends Throwable> String writeToString(ThrowConsumer<Writer, E> consumer) throws E {
        return writeToString(consumer, DEFAULT_SIZE_CHARS);
    }

    @NotNull
    public static <E extends Throwable> String writeToString(ThrowConsumer<Writer, E> consumer, int size) throws E {
        Writer writer = new StringWriter(size);
        consumer.accept(writer);
        return writer.toString();
    }

    public static <E extends Throwable> byte[] writeToBytes(ThrowConsumer<Writer, E> consumer) throws E {
        return writeToBytes(consumer, DEFAULT_SIZE_BYTES);
    }

    public static <E extends Throwable> byte[] writeToBytes(ThrowConsumer<Writer, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        Writer writer = new OutputStreamWriter(output);
        consumer.accept(writer);
        closeQuietly(writer);
        return output.toByteArray();
    }

    public static <E extends Throwable> byte[] outputToBytes(ThrowConsumer<OutputStream, E> consumer) throws E {
        return outputToBytes(consumer, DEFAULT_SIZE_BYTES);
    }

    public static <E extends Throwable> byte[] outputToBytes(ThrowConsumer<OutputStream, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        consumer.accept(output);
        return output.toByteArray();
    }

    public static <T, K, V, E extends Throwable> Map<K, V> castMapOrFail(T object, Function<T, E> error) throws E {
        if (object instanceof Map<?, ?> map) {
            return Casting.castMap(map);
        }
        throw error.apply(object);
    }

    // TODO: extract utils below:
    // IOUtil

    @SuppressWarnings("UnstableApiUsage")
    public static void closeQuietly(@Nullable Closeable closeable) {
        try {
            Closeables.close(closeable, true);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void closeRethrow(@Nullable Closeable closeable) {
        try {
            Closeables.close(closeable, false);
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }
}
