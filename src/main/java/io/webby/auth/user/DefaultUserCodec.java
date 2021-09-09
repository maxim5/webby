package io.webby.auth.user;

import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.webby.db.codec.Codecs.*;

public class DefaultUserCodec implements Codec<DefaultUser> {
    @Override
    public @NotNull CodecSize size() {
        return CodecSize.fixed(INT64_SIZE + INT16_SIZE);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull DefaultUser instance) throws IOException {
        return writeLong64(instance.userId(), output) +
               writeInt16(instance.access().level(), output);
    }

    @Override
    public @NotNull DefaultUser readFrom(@NotNull InputStream input, int available) throws IOException {
        long userId = readLong64(input);
        int level = readInt16(input);
        return new DefaultUser(userId, new UserAccess(level));
    }
}
