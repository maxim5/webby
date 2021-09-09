package io.webby.db.codec;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import okio.Buffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.webby.db.codec.Codecs.*;
import static io.webby.testing.TestingBytes.CHARSET;
import static io.webby.testing.TestingBytes.assertBytes;
import static io.webby.testing.TestingParams.paramToBytes;
import static io.webby.testing.TestingParams.paramToString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecsTest {
    private final Buffer buffer = new Buffer();

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Byte.MAX_VALUE, Byte.MIN_VALUE})
    public void primitives_byte(int value) throws Exception {
        assertEquals(Codecs.INT8_SIZE, writeByte8(value, buffer.outputStream()));
        assertEquals(Codecs.INT8_SIZE, buffer.size());
        assertEquals("%02x".formatted((byte) value), buffer.snapshot().hex());
        assertBytes(buffer.snapshot().toByteArray(), new byte[]{(byte) value});
        assertEquals(value, readByte8(buffer.inputStream()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Short.MAX_VALUE, Short.MIN_VALUE})
    public void primitives_short(int value) throws Exception {
        assertEquals(Codecs.INT16_SIZE, writeInt16(value, buffer.outputStream()));
        assertEquals(Codecs.INT16_SIZE, buffer.size());
        assertEquals("%04x".formatted((short) value), buffer.snapshot().hex());
        assertBytes(buffer.snapshot().toByteArray(), Shorts.toByteArray((short) value));
        assertEquals(value, readInt16(buffer.inputStream()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 12, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void primitives_int(int value) throws Exception {
        assertEquals(Codecs.INT32_SIZE, writeInt32(value, buffer.outputStream()));
        assertEquals(Codecs.INT32_SIZE, buffer.size());
        assertEquals("%08x".formatted(value), buffer.snapshot().hex());
        assertBytes(buffer.snapshot().toByteArray(), Ints.toByteArray(value));
        assertEquals(value, readInt32(buffer.inputStream()));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 12, -1, Long.MAX_VALUE, Long.MIN_VALUE})
    public void primitives_long(long value) throws Exception {
        assertEquals(Codecs.INT64_SIZE, writeLong64(value, buffer.outputStream()));
        assertEquals(Codecs.INT64_SIZE, buffer.size());
        assertEquals("%016x".formatted(value), buffer.snapshot().hex());
        assertBytes(buffer.snapshot().toByteArray(), Longs.toByteArray(value));
        assertEquals(value, readLong64(buffer.inputStream()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void primitives_long(boolean value) throws Exception {
        assertEquals(Codecs.INT8_SIZE, writeBoolean8(value, buffer.outputStream()));
        assertEquals(Codecs.INT8_SIZE, buffer.size());
        assertEquals("%02x".formatted(value ? 1 : 0), buffer.snapshot().hex());
        assertBytes(buffer.snapshot().toByteArray(), new byte[]{(byte) (value ? 1 : 0)});
        assertEquals(value, readBoolean8(buffer.inputStream()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "null", "0, 0, 0", "-1, -2, -3", "127, 127, 127", "-128, -128, -128"})
    public void byte_arrays(String encoded) throws Exception {
        byte[] bytes = paramToBytes(encoded);

        assertEquals(nullableByteArraySize(bytes), writeNullableByteArray(bytes, buffer.outputStream()));
        assertBytes(readNullableByteArray(buffer.inputStream()), bytes);

        assertEquals(shortNullableByteArraySize(bytes), writeShortNullableByteArray(bytes, buffer.outputStream()));
        assertBytes(readShortNullableByteArray(buffer.inputStream()), bytes);

        if (bytes != null) {
            assertEquals(byteArraySize(bytes), writeByteArray(bytes, buffer.outputStream()));
            assertBytes(readByteArray(buffer.inputStream()), bytes);

            assertEquals(shortByteArraySize(bytes), writeShortByteArray(bytes, buffer.outputStream()));
            assertBytes(readShortByteArray(buffer.inputStream()), bytes);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "foo", "0", "0,1,2,3"})
    public void strings(String encoded) throws Exception {
        String input = paramToString(encoded);

        assertEquals(nullableStringSize(input, CHARSET), writeNullableString(input, CHARSET, buffer.outputStream()));
        assertEquals(input, readNullableString(buffer.inputStream(), CHARSET));

        assertEquals(shortNullableStringSize(input, CHARSET), writeShortNullableString(input, CHARSET, buffer.outputStream()));
        assertEquals(input, readShortNullableString(buffer.inputStream(), CHARSET));

        if (input != null) {
            assertEquals(stringSize(input, CHARSET), writeString(input, CHARSET, buffer.outputStream()));
            assertEquals(input, readString(buffer.inputStream(), CHARSET));

            assertEquals(shortStringSize(input, CHARSET), writeShortString(input, CHARSET, buffer.outputStream()));
            assertEquals(input, readShortString(buffer.inputStream(), CHARSET));
        }
    }
}
