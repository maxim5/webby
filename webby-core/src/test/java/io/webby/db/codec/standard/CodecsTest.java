package io.webby.db.codec.standard;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import okio.Buffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.codec.standard.Codecs.*;
import static io.webby.testing.TestingBytes.CHARSET;
import static io.webby.testing.TestingBytes.assertBytes;
import static io.webby.testing.TestingParams.paramToBytes;
import static io.webby.testing.TestingParams.paramToString;

public class CodecsTest {
    private final Buffer buffer = new Buffer();

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Byte.MAX_VALUE, Byte.MIN_VALUE})
    public void primitives_byte(int value) throws Exception {
        assertThat(writeByte8(value, buffer.outputStream())).isEqualTo(INT8_SIZE);
        assertThat(buffer.size()).isEqualTo(INT8_SIZE);
        assertThat(buffer.snapshot().hex()).isEqualTo("%02x".formatted((byte) value));
        assertBytes(buffer.snapshot().toByteArray()).isEqualTo(new byte[]{(byte) value});
        assertThat(readByte8(buffer.inputStream())).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Short.MAX_VALUE, Short.MIN_VALUE})
    public void primitives_short(int value) throws Exception {
        assertThat(writeInt16(value, buffer.outputStream())).isEqualTo(INT16_SIZE);
        assertThat(buffer.size()).isEqualTo(INT16_SIZE);
        assertThat(buffer.snapshot().hex()).isEqualTo("%04x".formatted((short) value));
        assertBytes(buffer.snapshot().toByteArray()).isEqualTo(Shorts.toByteArray((short) value));
        assertThat(readInt16(buffer.inputStream())).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void primitives_int(int value) throws Exception {
        assertThat(writeInt32(value, buffer.outputStream())).isEqualTo(INT32_SIZE);
        assertThat(buffer.size()).isEqualTo(INT32_SIZE);
        assertThat(buffer.snapshot().hex()).isEqualTo("%08x".formatted(value));
        assertBytes(buffer.snapshot().toByteArray()).isEqualTo(Ints.toByteArray(value));
        assertThat(readInt32(buffer.inputStream())).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 12, -1, Long.MAX_VALUE, Long.MIN_VALUE})
    public void primitives_long(long value) throws Exception {
        assertThat(writeLong64(value, buffer.outputStream())).isEqualTo(INT64_SIZE);
        assertThat(buffer.size()).isEqualTo(INT64_SIZE);
        assertThat(buffer.snapshot().hex()).isEqualTo("%016x".formatted(value));
        assertBytes(buffer.snapshot().toByteArray()).isEqualTo(Longs.toByteArray(value));
        assertThat(readLong64(buffer.inputStream())).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void primitives_long(boolean value) throws Exception {
        assertThat(writeBoolean8(value, buffer.outputStream())).isEqualTo(INT8_SIZE);
        assertThat(buffer.size()).isEqualTo(INT8_SIZE);
        assertThat(buffer.snapshot().hex()).isEqualTo("%02x".formatted(value ? 1 : 0));
        assertBytes(buffer.snapshot().toByteArray()).isEqualTo(new byte[]{(byte) (value ? 1 : 0)});
        assertThat(readBoolean8(buffer.inputStream())).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "null", "0, 0, 0", "-1, -2, -3", "127, 127, 127", "-128, -128, -128"})
    public void byte_arrays(String encoded) throws Exception {
        byte[] bytes = paramToBytes(encoded);

        assertThat(writeNullableByteArray(bytes, buffer.outputStream())).isEqualTo(nullableByteArraySize(bytes));
        assertBytes(readNullableByteArray(buffer.inputStream())).isEqualTo(bytes);

        assertThat(writeShortNullableByteArray(bytes, buffer.outputStream())).isEqualTo(shortNullableByteArraySize(bytes));
        assertBytes(readShortNullableByteArray(buffer.inputStream())).isEqualTo(bytes);

        if (bytes != null) {
            assertThat(writeByteArray(bytes, buffer.outputStream())).isEqualTo(byteArraySize(bytes));
            assertBytes(readByteArray(buffer.inputStream())).isEqualTo(bytes);

            assertThat(writeShortByteArray(bytes, buffer.outputStream())).isEqualTo(shortByteArraySize(bytes));
            assertBytes(readShortByteArray(buffer.inputStream())).isEqualTo(bytes);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "foo", "0", "0,1,2,3"})
    public void strings(String encoded) throws Exception {
        String input = paramToString(encoded);

        assertThat(writeNullableString(input, CHARSET, buffer.outputStream())).isEqualTo(nullableStringSize(input, CHARSET));
        assertThat(readNullableString(buffer.inputStream(), CHARSET)).isEqualTo(input);

        assertThat(writeShortNullableString(input, CHARSET, buffer.outputStream())).isEqualTo(shortNullableStringSize(input, CHARSET));
        assertThat(readShortNullableString(buffer.inputStream(), CHARSET)).isEqualTo(input);

        if (input != null) {
            assertThat(writeString(input, CHARSET, buffer.outputStream())).isEqualTo(stringSize(input, CHARSET));
            assertThat(readString(buffer.inputStream(), CHARSET)).isEqualTo(input);

            assertThat(writeShortString(input, CHARSET, buffer.outputStream())).isEqualTo(shortStringSize(input, CHARSET));
            assertThat(readShortString(buffer.inputStream(), CHARSET)).isEqualTo(input);
        }
    }
}
