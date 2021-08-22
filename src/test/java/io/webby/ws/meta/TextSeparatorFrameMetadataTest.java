package io.webby.ws.meta;

import io.webby.netty.ws.Constants;
import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TextSeparatorFrameMetadataTest {
    @Test
    public void parse_simple() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo 123 bar"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(123, requestId);
            assertByteBuf(content, "bar");
        });
    }

    @Test
    public void parse_negative_id() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo -123 bar"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(-123, requestId);
            assertByteBuf(content, "bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("x 0 y"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "x");
            assertEquals(0, requestId);
            assertByteBuf(content, "y");
        });
    }

    @Test
    public void parse_empty_content() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo 777 "), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(777, requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_fail_to_parse_request_id() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo xxx bar"), (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "bar");
        });
    }

    @Test
    public void parse_invalid_empty() {
        new TextSeparatorFrameMetadata().parse(asByteBuf(""), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "");
        });
    }

    @Test
    public void parse_invalid_empty_acceptorId() {
        new TextSeparatorFrameMetadata().parse(asByteBuf(" 123 bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, " 123 bar");
        });
    }

    @Test
    public void parse_invalid_too_long_acceptorId() {
        new TextSeparatorFrameMetadata((byte) ' ', 3).parse(asByteBuf("foobar 0 bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foobar 0 bar");
        });
    }

    @Test
    public void parse_invalid_no_separators() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo123bar"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo123bar");
        });
    }

    @Test
    public void parse_invalid_no_request_id() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo "), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo ");
        });
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo 123"), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "foo 123");
        });
    }

    @Test
    public void parse_only_separators() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("             "), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, "             ");
        });
    }
}
