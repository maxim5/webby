package io.webby.db.kv.chronicle;

import io.webby.db.codec.Codec;
import io.webby.util.Rethrow;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

final class BytesReaderWriter<T> implements BytesReader<T>, BytesWriter<T> {
    private final @NotNull Codec<T> codec;

    BytesReaderWriter(@NotNull Codec<T> codec) {
        this.codec = codec;
    }

    @NotNull
    @Override
    public T read(Bytes in, @Nullable T using) {
        try {
            return codec.readFrom(in.inputStream(), (int) in.readRemaining());
        } catch (IOException e) {
            return Rethrow.rethrow("Failed to read bytes using %s".formatted(codec), e);
        }
    }

    @Override
    public void write(Bytes out, @NotNull T toWrite) {
        try {
            codec.writeTo(out.outputStream(), toWrite);
        } catch (IOException e) {
            Rethrow.rethrow("Failed to write bytes using %s".formatted(codec), e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BytesReaderWriter<?> that && Objects.equals(this.codec, that.codec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codec);
    }

    @Override
    public String toString() {
        return "BytesReaderWriter[codec=%s]".formatted(codec);
    }
}
