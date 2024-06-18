package io.spbx.webby.db.codec.standard;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.spbx.webby.db.codec.AssertCodec.assertCodec;

public class Instant96CodecTest {
    @Test
    public void instant_roundtrip() throws Exception {
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.now());
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(0L));
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(2_000_000_000_000L));
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(4_000_000_000_000L));
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.parse("2500-01-01T00:00:00.000000000Z"));
        assertCodec(Instant96Codec.INSTANCE).roundtrip(Instant.parse("3000-01-01T00:00:00.000000000Z"));
    }
}
