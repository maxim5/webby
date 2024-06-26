package io.spbx.webby.ws.meta;

import com.google.common.primitives.Longs;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.*;
import static io.spbx.webby.testing.ws.meta.AssertMeta.assertNotParsed;

public class BinarySeparatorFrameMetadataTest {
    @Test
    public void parse_simple() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo 00000000 bar"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("00000000")));
            assertBytes(content).isEqualTo("bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("x ~~~~~~~~ y"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("x");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("~~~~~~~~")));
            assertBytes(content).isEqualTo("y");
        });
    }

    @Test
    public void parse_empty_content() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo !@#$%^&* "), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("!@#$%^&*")));
            assertBytes(content).isEqualTo("");
        });
    }

    @Test
    public void parse_all_separators() {
        new BinarySeparatorFrameMetadata().parse(asByteBuf("foo             "), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("        ")));
            assertBytes(content).isEqualTo("   ");
        });
    }

    @Test
    public void parse_invalid_empty() {
        assertNotParsed("", new BinarySeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_empty_acceptorId() {
        assertNotParsed(" 00000000 bar", new BinarySeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_too_long_acceptorId() {
        assertNotParsed("foobar 00000000 bar", new BinarySeparatorFrameMetadata((byte) ' ', 3));
    }

    @Test
    public void parse_invalid_no_separators() {
        assertNotParsed("foo00000000bar", new BinarySeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_no_request_id() {
        assertNotParsed("foo ", new BinarySeparatorFrameMetadata());
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        assertNotParsed("foo 00000000", new BinarySeparatorFrameMetadata());
    }

    @Test
    public void parse_only_separators() {
        assertNotParsed("             ", new BinarySeparatorFrameMetadata());
    }
}
