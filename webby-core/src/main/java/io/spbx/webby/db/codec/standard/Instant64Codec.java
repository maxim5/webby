package io.spbx.webby.db.codec.standard;

import io.spbx.util.time.NanoTimestamp;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import static io.spbx.webby.db.codec.standard.Codecs.*;

public class Instant64Codec implements Codec<Instant> {
    public static final Instant64Codec INSTANCE = new Instant64Codec();

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.fixed(INT64_SIZE);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Instant instance) throws IOException {
        return writeLong64(NanoTimestamp.instantToLongNano(instance), output);
    }

    @Override
    public @NotNull Instant readFrom(@NotNull InputStream input, int available) throws IOException {
        return NanoTimestamp.longNanoToInstant(readLong64(input));
    }
}
