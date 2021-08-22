package io.webby.ws.meta;

import com.google.common.primitives.Longs;
import io.webby.netty.ws.Constants;
import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBytes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BinaryFixedSizeFrameMetadataTest {
    @Test
    public void parse_simple() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo00000000bar"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("00000000")), requestId);
            assertByteBuf(content, "bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new BinaryFixedSizeFrameMetadata(1).parse(asByteBuf("x~~~~~~~~y"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "x");
            assertEquals(Longs.fromByteArray(asBytes("~~~~~~~~")), requestId);
            assertByteBuf(content, "y");
        });
    }

    @Test
    public void parse_empty_content() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo!@#$%^&*"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("!@#$%^&*")), requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_all_separators() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo           "), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("        ")), requestId);
            assertByteBuf(content, "   ");
        });
    }

    @Test
    public void parse_invalid_empty() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf(""), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("xx12345678"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "xx12345678");
        });
    }
}
