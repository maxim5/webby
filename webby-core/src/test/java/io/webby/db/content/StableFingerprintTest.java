package io.webby.db.content;

import io.webby.testing.Testing;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.webby.db.content.StableFingerprint.BitSize.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StableFingerprintTest {
    private final StableFingerprint fingerprint = new StableFingerprint(Testing.defaultAppSettings());

    @ParameterizedTest
    @ValueSource(strings = {"", "x", "foo", "foo-bar-baz", "1", "65536"})
    public void computeFingerprint_size_simple(String input) {
        byte[] bytes = parseByteArray(input);
        assertContentId(fingerprint.computeFingerprint(SIZE_128, bytes), 128);
        assertContentId(fingerprint.computeFingerprint(SIZE_160, bytes), 160);
        assertContentId(fingerprint.computeFingerprint(SIZE_256, bytes), 256);
        assertContentId(fingerprint.computeFingerprint(SIZE_512, bytes), 512);
    }

    @Test
    public void computeFingerprint_stable() {
        assertEquals("kHskmktnxTtrgajuJNM71w", fingerprint.computeFingerprint(SIZE_128, "".getBytes()).contentId());
        assertEquals("YE5FKkIM1dbValXXRZWrag", fingerprint.computeFingerprint(SIZE_128, "foo".getBytes()).contentId());
        assertEquals("F6ep4XFFCgfAOXEsELlGZA", fingerprint.computeFingerprint(SIZE_128, "12345".getBytes()).contentId());
        assertEquals("px0XT6azvh6oxFvgKCSibA", fingerprint.computeFingerprint(SIZE_128, "12346".getBytes()).contentId());
    }

    private static void assertContentId(ContentId contentId, int bits) {
        int expectedFingerprintSize = (bits + 5) / 6;
        assertEquals(expectedFingerprintSize, contentId.contentId().length());
    }

    private static byte[] parseByteArray(String input) {
        try {
            int length = Integer.parseInt(input);
            return new byte[length];
        } catch (NumberFormatException e) {
            return input.getBytes();
        }
    }
}
