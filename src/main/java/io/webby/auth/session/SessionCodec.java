package io.webby.auth.session;

import com.google.inject.Inject;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import io.webby.db.codec.standard.Instant64Codec;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;

import static io.webby.db.codec.standard.Codecs.*;

public class SessionCodec implements Codec<Session> {
    public static final SessionCodec DEFAULT_INSTANCE = new SessionCodec(Instant64Codec.INSTANCE);

    private final Codec<Instant> instantCodec;
    @Inject private Charset charset;

    public SessionCodec(@NotNull Codec<Instant> instantCodec) {
        this.instantCodec = instantCodec;
    }

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.averageSize(160);   // 2 * 8 + 4 + 128 + 12
    }

    @Override
    public int sizeOf(@NotNull Session instance) {
        return INT64_SIZE + INT32_SIZE + instantCodec.size().numBytes() +
               stringSize(instance.userAgent(), charset) +
               nullableStringSize(instance.ipAddress(), charset);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Session instance) throws IOException {
        return writeLong64(instance.sessionId(), output) +
               writeInt32(instance.userId(), output) +
               instantCodec.writeTo(output, instance.created()) +
               writeString(instance.userAgent(), charset, output) +
               writeNullableString(instance.ipAddress(), charset, output);
    }

    @Override
    public @NotNull Session readFrom(@NotNull InputStream input, int available) throws IOException {
        long sessionId = readLong64(input);
        int userId = readInt32(input);
        Instant created = instantCodec.readFrom(input, available);
        String userAgent = readString(input, charset);
        String ipAddress = readNullableString(input, charset);
        return new Session(sessionId, ForeignInt.ofId(userId), created, userAgent, ipAddress);
    }
}
