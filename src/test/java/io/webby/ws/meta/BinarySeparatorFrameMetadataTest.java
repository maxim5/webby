package io.webby.ws.meta;

import com.google.common.primitives.Longs;
import io.webby.netty.ws.Constants;
import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBytes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BinarySeparatorFrameMetadataTest {
    @Test
    public void parse_simple() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo 00000000 bar"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("00000000")), requestId);
            assertByteBuf(content, "bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("x ~~~~~~~~ y"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "x");
            assertEquals(Longs.fromByteArray(asBytes("~~~~~~~~")), requestId);
            assertByteBuf(content, "y");
        });
    }

    @Test
    public void parse_empty_content() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo !@#$%^&* "), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("!@#$%^&*")), requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_all_separators() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo             "), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Longs.fromByteArray(asBytes("        ")), requestId);
            assertByteBuf(content, "   ");
        });
    }

    @Test
    public void parse_invalid_empty() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf(""), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_invalid_empty_acceptorId() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf(" 00000000 bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, " 00000000 bar");
        });
    }

    @Test
    public void parse_invalid_too_long_acceptorId() {
        new BinarySeparatorFrameMetadata((byte) ' ', 3)
                .parse(asByteBuf("foobar 00000000 bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foobar 00000000 bar");
        });
    }

    @Test
    public void parse_invalid_no_separators() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo00000000bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo00000000bar");
        });
    }

    @Test
    public void parse_invalid_no_request_id() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo "), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo ");
        });
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo 00000000"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo 00000000");
        });
    }

    @Test
    public void parse_only_separators() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("             "), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "             ");
        });
    }
}
