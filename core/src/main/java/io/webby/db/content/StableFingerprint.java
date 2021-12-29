package io.webby.db.content;

import com.google.inject.Inject;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static io.webby.util.base.Rethrow.rethrow;

public class StableFingerprint {
    private static final byte[] DEFAULT_BYTES = {77, -9, 18, 41};
    private final byte[] key;

    @Inject
    public StableFingerprint(@NotNull Settings settings) {
        key = xor(settings.securityKey(), DEFAULT_BYTES);
    }

    public @NotNull ContentId computeFingerprint(@NotNull BitSize bitSize, byte @NotNull[] content) {
        MessageDigest instance = bitSize.getMessageDigest();
        instance.update(key);
        byte[] digest = instance.digest(content);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        return new ContentId(encoded);
    }

    private static byte @NotNull[] xor(byte @NotNull[] input, byte @NotNull[] key) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return output;
    }

    public enum BitSize {
        SIZE_128("MD5"),
        SIZE_160("SHA"),
        SIZE_256("SHA256"),
        SIZE_512("SHA512");

        private final String algorithm;

        BitSize(String algorithm) {
            this.algorithm = algorithm;
        }

        private @NotNull MessageDigest getMessageDigest() {
            try {
                return MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                return rethrow(e);
            }
        }
    }
}
