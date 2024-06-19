package io.spbx.webby.url.view;

import io.spbx.util.base.EasyCast;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.common.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;
import java.util.function.Function;

import static io.spbx.util.io.EasyIo.Close.closeQuietly;

public class EasyRender {
    public static <E extends Throwable>
            @NotNull String writeToString(@NotNull ThrowConsumer<Writer, E> consumer) throws E {
        return writeToString(consumer, SystemProperties.DEFAULT_SIZE_CHARS);
    }

    public static <E extends Throwable>
            @NotNull String writeToString(@NotNull ThrowConsumer<Writer, E> consumer, int size) throws E {
        Writer writer = new StringWriter(size);
        consumer.accept(writer);
        return writer.toString();
    }

    public static <E extends Throwable>
            byte @NotNull [] writeToBytes(@NotNull ThrowConsumer<Writer, E> consumer) throws E {
        return writeToBytes(consumer, SystemProperties.DEFAULT_SIZE_BYTES);
    }

    public static <E extends Throwable>
            byte @NotNull [] writeToBytes(@NotNull ThrowConsumer<Writer, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        Writer writer = new OutputStreamWriter(output);
        consumer.accept(writer);
        closeQuietly(writer);
        return output.toByteArray();
    }

    public static <E extends Throwable>
            byte @NotNull [] outputToBytes(@NotNull ThrowConsumer<OutputStream, E> consumer) throws E {
        return outputToBytes(consumer, SystemProperties.DEFAULT_SIZE_CHARS);
    }

    public static <E extends Throwable>
            byte @NotNull [] outputToBytes(@NotNull ThrowConsumer<OutputStream, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        consumer.accept(output);
        return output.toByteArray();
    }

    public static <T, K, V, E extends Throwable>
            @NotNull Map<K, V> castMapOrFail(@Nullable T object, @NotNull Function<T, E> error) throws E {
        if (object instanceof Map<?, ?> map) {
            return EasyCast.castMap(map);
        }
        throw error.apply(object);
    }
}
