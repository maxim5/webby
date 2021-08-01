package io.webby.db.codec;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Aka Serializer
public interface Codec<T> {
    @CanIgnoreReturnValue
    int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException;

    @NotNull
    T readFrom(@NotNull InputStream input, int available) throws IOException;
}
