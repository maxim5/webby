package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer<T> {
    int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException;

    @NotNull
    T readFrom(@NotNull InputStream input, int available) throws IOException;
}
