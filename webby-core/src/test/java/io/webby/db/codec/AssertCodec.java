package io.webby.db.codec;

import com.google.common.io.ByteStreams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.assertBytes;

public class AssertCodec {
    @CheckReturnValue
    public static <T> @NotNull CodecSubject<T> assertCodec(@NotNull Codec<T> codec) {
        return new CodecSubject<>(codec);
    }

    @CanIgnoreReturnValue
    public record CodecSubject<T>(@NotNull Codec<T> codec) {
        public @NotNull CodecSubject<T> roundtrip(@NotNull T value) throws IOException {
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
            assertBytes(prefix).isEqualTo(Arrays.copyOfRange(prefixedBytes, 0, prefix.length));
            assertBytes(bytes).isEqualTo(Arrays.copyOfRange(prefixedBytes, prefix.length, prefixedBytes.length));
            assertThat(value).isEqualTo(codec.readFromBytes(prefix.length, prefixedBytes));
            assertSize(prefixedBytes.length - prefix.length, predictedSize);

            return this;
        }

        private static void assertSize(int length, int predictedSize) {
            if (predictedSize >= 0) {
                assertThat(length).isEqualTo(predictedSize);
            }
        }
    }
}
