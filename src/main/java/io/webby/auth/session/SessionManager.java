package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.app.Settings;
import io.webby.util.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

public class SessionManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SecureRandom secureRandom;
    private final Cipher cipher;
    private final Cipher decipher;

    @Inject
    public SessionManager(@NotNull Settings settings) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        cipher = Cipher.getInstance("AES");
        decipher = Cipher.getInstance("AES");

        String serverKey = "0123456789123456";  // TODO: to the settings
        Key key = new SecretKeySpec(serverKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        decipher.init(Cipher.DECRYPT_MODE, key);
    }

    @NotNull
    public Session getOrCreateSession(@Nullable Cookie cookie) {
        Session session = getSessionOrNull(cookie);
        if (session == null) {
            return createNewSession();
        }
        return session;
    }

    @Nullable
    public Session getSessionOrNull(@Nullable Cookie cookie) {
        if (cookie == null) {
            return null;
        }
        try {
            long sessionId = decodeCookie(cookie);
            return new Session(sessionId, Instant.now());   // TODO: get from storage
        } catch (Throwable throwable) {
            log.at(Level.WARNING).withCause(throwable).log("Failed to decode a cookie: %s".formatted(cookie));
            return null;
        }
    }

    @NotNull
    public Session createNewSession() {
        long randomLong = secureRandom.nextLong();
        return new Session(randomLong, Instant.now());  // TODO: persist
    }

    @NotNull
    public String encodeSession(Session session) {
        return encodeSessionId(session.sessionId());
    }

    private String encodeSessionId(long sessionId) {
        byte[] bytes = Longs.toByteArray(sessionId);
        byte[] encryptedBytes = encryptBytes(bytes);
        return Base64.getUrlEncoder().encodeToString(encryptedBytes);
    }

    private long decodeCookie(@NotNull Cookie cookie) {
        byte[] encryptedBytes = Base64.getUrlDecoder().decode(cookie.value());
        byte[] decryptedBytes = decryptBytes(encryptedBytes);
        return Longs.fromByteArray(decryptedBytes);
    }

    private byte[] encryptBytes(byte[] data) {
        try {
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            return Rethrow.rethrow("Failed to encrypt the data: %s".formatted(Arrays.toString(data)), e);
        }
    }

    private byte[] decryptBytes(byte[] encrypted) {
        try {
            return decipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return Rethrow.rethrow("Failed to decrypt the data: %s".formatted(Arrays.toString(encrypted)), e);
        }
    }
}
