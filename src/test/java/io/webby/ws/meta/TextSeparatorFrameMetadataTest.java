package io.webby.ws.meta;

import io.webby.netty.ws.Constants;
import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static io.webby.ws.meta.AssertMeta.assertNotParsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertNotParsed("", new TextSeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_empty_acceptorId() {
        assertNotParsed(" 123 bar", new TextSeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_too_long_acceptorId() {
        assertNotParsed("foobar 0 bar", new TextSeparatorFrameMetadata((byte) ' ', 3));
    }

    @Test
    public void parse_invalid_no_separators() {
        assertNotParsed("foo123bar", new TextSeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_no_request_id() {
        assertNotParsed("foo ", new TextSeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        assertNotParsed("foo 123", new TextSeparatorFrameMetadata());
    }

    @Test
    public void parse_only_separators() {
        assertNotParsed("             ", new TextSeparatorFrameMetadata());
    }
}
