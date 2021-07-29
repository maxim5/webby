package io.webby.auth.session;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.webby.db.kv.Serializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

public class SessionSerializer implements Serializer<Session> {
    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Session instance) throws IOException {
        output.write(Longs.toByteArray(instance.sessionId()));  // 8
        output.write(Longs.toByteArray(instance.created().getEpochSecond()));   // 8
        output.write(Ints.toByteArray(instance.created().getNano()));  // 4
        return 20;
    }

    @Override
    public @NotNull Session readFrom(@NotNull InputStream input, int available) throws IOException {
        long sessionId = Longs.fromByteArray(input.readNBytes(8));
        long seconds = Longs.fromByteArray(input.readNBytes(8));
        int nanos = Ints.fromByteArray(input.readNBytes(4));
        return new Session(sessionId, Instant.ofEpochSecond(seconds, nanos));
    }
}
