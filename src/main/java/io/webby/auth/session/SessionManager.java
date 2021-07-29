package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.app.Settings;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueDbFactory;
import io.webby.util.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

public class SessionManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SecureRandom secureRandom;
    private final Cipher cipher;
    private final Cipher decipher;
    private final KeyValueDb<Long, Session> db;

    @Inject
    public SessionManager(@NotNull Settings settings, @NotNull KeyValueDbFactory dbFactory) throws Exception {
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        cipher = Cipher.getInstance("AES");
        decipher = Cipher.getInstance("AES");

        Key key = new SecretKeySpec(settings.securityKey(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        decipher.init(Cipher.DECRYPT_MODE, key);

        db = dbFactory.getDb("sessions", Long.class, Session.class);
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
            long sessionId = decodeSessionId(cookie.value());
            return db.get(sessionId);
        } catch (Throwable throwable) {
            log.at(Level.WARNING).withCause(throwable).log("Failed to decode a cookie: %s".formatted(cookie));
            return null;
        }
    }

    @NotNull
    public Session createNewSession() {
        long randomLong = secureRandom.nextLong();
        Session session = new Session(randomLong, Instant.now());
        db.set(session.sessionId(), session);
        return session;
    }

    @NotNull
    public String encodeSession(@NotNull Session session) {
        return encodeSessionId(session.sessionId());
    }

    @VisibleForTesting
    @NotNull
    String encodeSessionId(long sessionId) {
        byte[] bytes = Longs.toByteArray(sessionId);
        byte[] encryptedBytes = encryptBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
    }

    @VisibleForTesting
    long decodeSessionId(@NotNull String value) {
        byte[] encryptedBytes = Base64.getUrlDecoder().decode(value);
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
            return Rethrow.rethrow("Failed to decrypt the data: %s".formatted(Arrays.toString(encrypted)), e);
        }
    }
}
