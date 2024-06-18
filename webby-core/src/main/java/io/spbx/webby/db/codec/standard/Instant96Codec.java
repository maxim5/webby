package io.spbx.webby.db.codec.standard;

import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import static io.spbx.webby.db.codec.standard.Codecs.*;

public class Instant96Codec implements Codec<Instant> {
    public static final Instant96Codec INSTANCE = new Instant96Codec();

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.fixed(INT64_SIZE + INT32_SIZE);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Instant instance) throws IOException {
        return writeLong64(instance.getEpochSecond(), output) +
               writeInt32(instance.getNano(), output);
    }

    @Override
    public @NotNull Instant readFrom(@NotNull InputStream input, int available) throws IOException {
        long seconds = readLong64(input);
        int nanos = readInt32(input);
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
