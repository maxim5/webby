package io.webby.auth.session;

import com.google.inject.Inject;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import io.webby.orm.api.ForeignLong;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;

import static io.webby.db.codec.Codecs.*;

public class SessionCodec implements Codec<Session> {
    @Inject private Charset charset;

    @Override
    public @NotNull CodecSize size() {
        return CodecSize.averageSize(160);   // 3 * 8 + 4 + 120 + 12
    }

    @Override
    public int sizeOf(@NotNull Session instance) {
        return INT64_SIZE * 3 + INT32_SIZE +
               stringSize(instance.userAgent(), charset) +
               nullableStringSize(instance.ipAddress(), charset);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Session instance) throws IOException {
        return writeLong64(instance.sessionId(), output) +
               writeLong64(instance.userId(), output) +
               writeLong64(instance.created().getEpochSecond(), output) +
               writeInt32(instance.created().getNano(), output) +
               writeString(instance.userAgent(), charset, output) +
               writeNullableString(instance.ipAddress(), charset, output);
    }

    @Override
    public @NotNull Session readFrom(@NotNull InputStream input, int available) throws IOException {
        long sessionId = readLong64(input);
        long userId = readLong64(input);
        long seconds = readLong64(input);
        int nanos = readInt32(input);
        String userAgent = readString(input, charset);
        String ipAddress = readNullableString(input, charset);
        return new Session(sessionId, ForeignLong.ofId(userId), Instant.ofEpochSecond(seconds, nanos), userAgent, ipAddress);
    }
}
