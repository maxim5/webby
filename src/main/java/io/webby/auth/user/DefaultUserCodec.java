package io.webby.auth.user;

import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import static io.webby.db.codec.Codecs.*;

public class DefaultUserCodec implements Codec<DefaultUser> {
    @Override
    public @NotNull CodecSize size() {
        return CodecSize.fixed(INT32_SIZE + INT64_SIZE + INT32_SIZE + INT16_SIZE);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull DefaultUser instance) throws IOException {
        return writeInt32(instance.userId(), output) +
               writeLong64(instance.created().getEpochSecond(), output) +
               writeInt32(instance.created().getNano(), output) +
               writeInt16(instance.access().level(), output);
    }

    @Override
    public @NotNull DefaultUser readFrom(@NotNull InputStream input, int available) throws IOException {
        int userId = readInt32(input);
        long seconds = readLong64(input);
        int nanos = readInt32(input);
        int level = readInt16(input);
        return new DefaultUser(userId, Instant.ofEpochSecond(seconds, nanos), new UserAccess(level));
    }
}
