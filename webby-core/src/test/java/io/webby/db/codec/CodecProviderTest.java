package io.webby.db.codec;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static io.webby.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecProviderTest {
    protected final CodecProvider provider = Testing.testStartup().getInstance(CodecProvider.class);

    @Test
    public void codecs_roundtrip() throws Exception {
        assertCodecRoundTrip(provider.getCodecOrDie(Integer.class), 0);
        assertCodecRoundTrip(provider.getCodecOrDie(Long.class), 0L);
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "");
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "foo");
        assertCodecRoundTrip(provider.getCodecOrDie(Session.class), TestingModels.newSessionNow(123));
        assertCodecRoundTrip(provider.getCodecOrDie(Session.class), TestingModels.newSessionNowWithoutIp(123));
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultUser.class), TestingModels.newUser(0));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static <T> void assertCodecRoundTrip(@NotNull Codec<T> codec, @NotNull T value) throws IOException {
        CodecSize size = codec.size();
        int predictedSize = codec.sizeOf(value);
        if (size.isFixed()) {
            assertEquals(size.numBytes(), predictedSize);
        }
        if (predictedSize >= 0) {
            int written = codec.writeTo(ByteStreams.nullOutputStream(), value);
            assertEquals(written, predictedSize);
        }

        byte[] bytes = codec.writeToBytes(value);
        assertSize(bytes.length, predictedSize);
        assertEquals(value, codec.readFromBytes(bytes));

        ByteBuffer buffer = codec.writeToByteBuffer(value);
        assertSize(buffer.remaining(), predictedSize);
        assertEquals(value, codec.readFromByteBuffer(buffer));

        ByteBuf byteBuf = codec.writeToByteBuf(value);
        assertSize(byteBuf.readableBytes(), predictedSize);
        assertEquals(value, codec.readFromByteBuf(byteBuf));

        byte[] prefix = { 0, 1, 2 };
        byte[] prefixedBytes = codec.writeToBytes(prefix, value);
        assertBytes(prefix, Arrays.copyOfRange(prefixedBytes, 0, prefix.length));
        assertBytes(bytes, Arrays.copyOfRange(prefixedBytes, prefix.length, prefixedBytes.length));
        assertEquals(value, codec.readFromBytes(prefix.length, prefixedBytes));
        assertSize(prefixedBytes.length - prefix.length, predictedSize);
    }

    private static void assertSize(int length, int predictedSize) {
        if (predictedSize >= 0) {
            assertEquals(length, predictedSize);
        }
    }
}
