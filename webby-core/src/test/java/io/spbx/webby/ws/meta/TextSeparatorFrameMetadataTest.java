package io.spbx.webby.ws.meta;

import io.spbx.webby.netty.ws.FrameConst;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.asByteBuf;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static io.spbx.webby.testing.ws.meta.AssertMeta.assertNotParsed;

public class TextSeparatorFrameMetadataTest {
    @Test
    public void parse_simple() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo 123 bar"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(123);
            assertBytes(content).isEqualTo("bar");
        });
    }

    @Test
    public void parse_negative_id() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo -123 bar"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(-123);
            assertBytes(content).isEqualTo("bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("x 0 y"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("x");
            assertThat(requestId).isEqualTo(0);
            assertBytes(content).isEqualTo("y");
        });
    }

    @Test
    public void parse_empty_content() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo 777 "), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(777);
            assertBytes(content).isEqualTo("");
        });
    }

    @Test
    public void parse_fail_to_parse_request_id() {
        new TextSeparatorFrameMetadata().parse(asByteBuf("foo xxx bar"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(FrameConst.RequestIds.NO_ID);
            assertBytes(content).isEqualTo("bar");
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
