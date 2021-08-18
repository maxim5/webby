package io.webby.url.view;

import io.webby.common.SystemProperties;
import io.webby.util.EasyCast;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;
import java.util.function.Function;

import static io.webby.util.EasyIO.Close.closeQuietly;

public class EasyRender {
    @NotNull
    public static <E extends Throwable> String writeToString(ThrowConsumer<Writer, E> consumer) throws E {
        return writeToString(consumer, SystemProperties.DEFAULT_SIZE_CHARS);
    }

    @NotNull
    public static <E extends Throwable> String writeToString(ThrowConsumer<Writer, E> consumer, int size) throws E {
        Writer writer = new StringWriter(size);
        consumer.accept(writer);
        return writer.toString();
    }

    public static <E extends Throwable> byte[] writeToBytes(ThrowConsumer<Writer, E> consumer) throws E {
        return writeToBytes(consumer, SystemProperties.DEFAULT_SIZE_BYTES);
    }

    public static <E extends Throwable> byte[] writeToBytes(ThrowConsumer<Writer, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        Writer writer = new OutputStreamWriter(output);
        consumer.accept(writer);
        closeQuietly(writer);
        return output.toByteArray();
    }

    public static <E extends Throwable> byte[] outputToBytes(ThrowConsumer<OutputStream, E> consumer) throws E {
        return outputToBytes(consumer, SystemProperties.DEFAULT_SIZE_CHARS);
    }

    public static <E extends Throwable> byte[] outputToBytes(ThrowConsumer<OutputStream, E> consumer, int size) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        consumer.accept(output);
        return output.toByteArray();
    }

    public static <T, K, V, E extends Throwable> Map<K, V> castMapOrFail(T object, Function<T, E> error) throws E {
        if (object instanceof Map<?, ?> map) {
            return EasyCast.castMap(map);
        }
        throw error.apply(object);
    }
}
