package io.spbx.webby.auth.user;

import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecSize;
import io.spbx.webby.db.codec.standard.Instant64Codec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import static io.spbx.webby.db.codec.standard.Codecs.*;

public class DefaultUserCodec implements Codec<DefaultUser> {
    public static final DefaultUserCodec DEFAULT_INSTANCE = new DefaultUserCodec(Instant64Codec.INSTANCE);

    private final Codec<Instant> instantCodec;

    public DefaultUserCodec(@NotNull Codec<Instant> instantCodec) {
        this.instantCodec = instantCodec;
    }

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.fixed(INT32_SIZE + instantCodec.size().numBytes() + INT16_SIZE);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull DefaultUser instance) throws IOException {
        return writeInt32(instance.userId(), output) +
               instantCodec.writeTo(output, instance.createdAt()) +
               writeInt16(instance.access().level(), output);
    }

    @Override
    public @NotNull DefaultUser readFrom(@NotNull InputStream input, int available) throws IOException {
        int userId = readInt32(input);
        Instant created = instantCodec.readFrom(input, available);
        int level = readInt16(input);
        return new DefaultUser(userId, created, new UserAccess(level));
    }
}
