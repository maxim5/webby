package io.webby.db.codec;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.testing.SessionBuilder;
import io.webby.testing.Testing;
import io.webby.testing.UserBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBytes.assertBytes;

public class CodecProviderTest {
    private final CodecProvider provider = Testing.testStartup().getInstance(CodecProvider.class);

    @Test
    public void codecs_roundtrip() throws Exception {
        assertCodecRoundTrip(provider.getCodecOrDie(Integer.class), 0);
        assertCodecRoundTrip(provider.getCodecOrDie(Long.class), 0L);
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "");
        assertCodecRoundTrip(provider.getCodecOrDie(String.class), "foo");
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultSession.class), SessionBuilder.ofId(123).build());
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultSession.class), SessionBuilder.ofId(123).withoutIpAddress().build());
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultUser.class), UserBuilder.ofId(456).build());
        assertCodecRoundTrip(provider.getCodecOrDie(DefaultUser.class), UserBuilder.ofAnyId(0).build());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static <T> void assertCodecRoundTrip(@NotNull Codec<T> codec, @NotNull T value) throws IOException {
        CodecSize size = codec.size();
        int predictedSize = codec.sizeOf(value);
        if (size.isFixed()) {
            assertThat(size.numBytes()).isEqualTo(predictedSize);
        }
        if (predictedSize >= 0) {
            int written = codec.writeTo(ByteStreams.nullOutputStream(), value);
            assertThat(written).isEqualTo(predictedSize);
        }

        byte[] bytes = codec.writeToBytes(value);
        assertSize(bytes.length, predictedSize);
        assertThat(codec.readFromBytes(bytes)).isEqualTo(value);

        ByteBuffer buffer = codec.writeToByteBuffer(value);
        assertSize(buffer.remaining(), predictedSize);
        assertThat(codec.readFromByteBuffer(buffer)).isEqualTo(value);

        ByteBuf byteBuf = codec.writeToByteBuf(value);
        assertSize(byteBuf.readableBytes(), predictedSize);
        assertThat(codec.readFromByteBuf(byteBuf)).isEqualTo(value);

        byte[] prefix = { 0, 1, 2 };
        byte[] prefixedBytes = codec.writeToBytes(prefix, value);
        assertBytes(prefix, Arrays.copyOfRange(prefixedBytes, 0, prefix.length));
        assertBytes(bytes, Arrays.copyOfRange(prefixedBytes, prefix.length, prefixedBytes.length));
        assertThat(value).isEqualTo(codec.readFromBytes(prefix.length, prefixedBytes));
        assertSize(prefixedBytes.length - prefix.length, predictedSize);
    }

    private static void assertSize(int length, int predictedSize) {
        if (predictedSize >= 0) {
            assertThat(length).isEqualTo(predictedSize);
        }
    }
}
