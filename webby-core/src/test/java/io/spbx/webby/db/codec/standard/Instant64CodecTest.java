package io.spbx.webby.db.codec.standard;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.spbx.webby.db.codec.AssertCodec.assertCodec;

public class Instant64CodecTest {
    @Test
    public void instant_roundtrip() throws Exception {
        assertCodec(Instant64Codec.INSTANCE).roundtrip(Instant.now());
        assertCodec(Instant64Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(0L));
        assertCodec(Instant64Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(2_000_000_000_000L));
        assertCodec(Instant64Codec.INSTANCE).roundtrip(Instant.ofEpochMilli(4_000_000_000_000L));
        assertCodec(Instant64Codec.INSTANCE).roundtrip(Instant.parse("2510-12-08T18:01:03.533116200Z"));
    }
}
