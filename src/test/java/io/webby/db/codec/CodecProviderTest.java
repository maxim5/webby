package io.webby.db.codec;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecProviderTest {
    protected final CodecProvider provider = Testing.testStartupNoHandlers().getInstance(CodecProvider.class);

    @Test
    public void codecs_roundtrip() {
        assertCodecRoundTrip(provider.getCodecOrDie(Integer.class), 0);
        assertCodecRoundTrip(provider.getCodecOrDie(Long.class), 0L);
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "");
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "foo");
        assertCodecRoundTrip(provider.getCodecOrDie(Session.class), new Session(0, 0, Instant.now(), "", null));
        assertCodecRoundTrip(provider.getCodecOrDie(Session.class), new Session(0, 0, Instant.now(), "User-Agent", "127.0.0.1"));
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultUser.class), new DefaultUser(0, UserAccess.Simple));
    }

    private static <T> void assertCodecRoundTrip(@NotNull Codec<T> codec, @NotNull T value) {
        byte[] bytes = codec.writeToBytes(value);
        T object = codec.readFromBytes(bytes);
        assertEquals(value, object);
    }
}
