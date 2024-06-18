package io.spbx.webby.db.kv.chronicle;

import io.spbx.util.base.Unchecked;
import io.spbx.webby.db.codec.Codec;
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

    @Override
    public @NotNull T read(Bytes in, @Nullable T using) {
        try {
            return codec.readFrom(in.inputStream(), (int) in.readRemaining());
        } catch (IOException e) {
            return Unchecked.rethrow("Failed to read bytes using %s".formatted(codec), e);
        }
    }

    @Override
    public void write(Bytes out, @NotNull T toWrite) {
        try {
            codec.writeTo(out.outputStream(), toWrite);
        } catch (IOException e) {
            Unchecked.rethrow("Failed to write bytes using %s".formatted(codec), e);
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
