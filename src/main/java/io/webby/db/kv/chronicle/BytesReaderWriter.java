package io.webby.db.kv.chronicle;

import io.webby.db.codec.Codec;
import io.webby.util.Rethrow;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

record BytesReaderWriter<T>(@NotNull Codec<T> codec) implements BytesReader<T>, BytesWriter<T> {
    @NotNull
    @Override
    public T read(Bytes in, @Nullable T using) {
        try {
            return codec.readFrom(in.inputStream(), (int) in.readRemaining());
        } catch (IOException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public void write(Bytes out, @NotNull T toWrite) {
        try {
            codec.writeTo(out.outputStream(), toWrite);
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }
}
