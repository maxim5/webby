package io.webby.auth.session;

import io.webby.db.serialize.Serializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import static io.webby.db.serialize.Serializers.*;

public class SessionSerializer implements Serializer<Session> {
    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull Session instance) throws IOException {
        return writeLong64(instance.sessionId(), output) +
               writeLong64(instance.userId(), output) +
               writeLong64(instance.created().getEpochSecond(), output) +
               writeInt32(instance.created().getNano(), output) +
               writeString(instance.userAgent(), output) +
               writeNullableString(instance.ipAddress(), output);
    }

    @Override
    public @NotNull Session readFrom(@NotNull InputStream input, int available) throws IOException {
        long sessionId = readLong64(input);
        long userId = readLong64(input);
        long seconds = readLong64(input);
        int nanos = readInt32(input);
        String userAgent = readString(input);
        String ipAddress = readNullableString(input);
        return new Session(sessionId, userId, Instant.ofEpochSecond(seconds, nanos), userAgent, ipAddress);
    }
}
