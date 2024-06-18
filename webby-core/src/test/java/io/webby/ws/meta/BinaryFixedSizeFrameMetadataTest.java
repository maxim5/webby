package io.webby.ws.meta;

import com.google.common.primitives.Longs;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.*;
import static io.webby.testing.ws.meta.AssertMeta.assertNotParsed;

public class BinaryFixedSizeFrameMetadataTest {
    @Test
    public void parse_simple() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo00000000bar"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("00000000")));
            assertBytes(content).isEqualTo("bar");
        });
    }

    @Test
    public void parse_short_acceptorId() {
        new BinaryFixedSizeFrameMetadata(1).parse(asByteBuf("x~~~~~~~~y"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("x");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("~~~~~~~~")));
            assertBytes(content).isEqualTo("y");
        });
    }

    @Test
    public void parse_empty_content() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo!@#$%^&*"), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("!@#$%^&*")));
            assertBytes(content).isEqualTo("");
        });
    }

    @Test
    public void parse_all_separators() {
        new BinaryFixedSizeFrameMetadata(3).parse(asByteBuf("foo           "), (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(Longs.fromByteArray(asBytes("        ")));
            assertBytes(content).isEqualTo("   ");
        });
    }

    @Test
    public void parse_invalid_empty() {
        assertNotParsed("", new BinaryFixedSizeFrameMetadata(3));
    }

    @Test
    public void parse_invalid_not_enough_bytes() {
        assertNotParsed("xx12345678", new BinaryFixedSizeFrameMetadata(3));
    }
}
