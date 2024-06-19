package io.spbx.webby.auth.session;

import com.google.inject.Inject;
import io.spbx.orm.api.ForeignInt;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecSize;
import io.spbx.webby.db.codec.standard.Instant64Codec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;

import static io.spbx.webby.db.codec.standard.Codecs.*;

public class DefaultSessionCodec implements Codec<DefaultSession> {
    public static final DefaultSessionCodec DEFAULT_INSTANCE = new DefaultSessionCodec(Instant64Codec.INSTANCE);

    private final Codec<Instant> instantCodec;
    @Inject private Charset charset;

    public DefaultSessionCodec(@NotNull Codec<Instant> instantCodec) {
        this.instantCodec = instantCodec;
    }

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.averageSize(160);   // 2 * 8 + 4 + 128 + 12
    }

    @Override
    public int sizeOf(@NotNull DefaultSession instance) {
        return INT64_SIZE + INT32_SIZE + instantCodec.size().numBytes() +
               stringSize(instance.userAgent(), charset) +
               nullableStringSize(instance.ipAddress(), charset);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull DefaultSession instance) throws IOException {
        return writeLong64(instance.sessionId(), output) +
               writeInt32(instance.userId(), output) +
               instantCodec.writeTo(output, instance.createdAt()) +
               writeString(instance.userAgent(), charset, output) +
               writeNullableString(instance.ipAddress(), charset, output);
    }

    @Override
    public @NotNull DefaultSession readFrom(@NotNull InputStream input, int available) throws IOException {
        long sessionId = readLong64(input);
        int userId = readInt32(input);
        Instant created = instantCodec.readFrom(input, available);
        String userAgent = readString(input, charset);
        String ipAddress = readNullableString(input, charset);
        return new DefaultSession(sessionId, ForeignInt.ofId(userId), created, userAgent, ipAddress);
    }
}
