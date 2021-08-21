package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.app.Settings;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.model.LongIdGenerator;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

import static io.webby.util.Rethrow.rethrow;

public class SessionManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Cipher cipher;
    private final Cipher decipher;
    private final LongIdGenerator generator;
    private final KeyValueDb<Long, Session> db;

    @Inject
    public SessionManager(@NotNull Settings settings, @NotNull KeyValueFactory factory) throws Exception {
        cipher = Cipher.getInstance("AES");
        decipher = Cipher.getInstance("AES");

        Key key = new SecretKeySpec(settings.securityKey(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        decipher.init(Cipher.DECRYPT_MODE, key);

        generator = LongIdGenerator.securePositiveRandom(SecureRandom.getInstance("SHA1PRNG"));
        db = factory.getDb("sessions", Long.class, Session.class);
    }

    public @NotNull Session getOrCreateSession(@NotNull HttpRequestEx request, @Nullable Cookie cookie) {
        Session session = getSessionOrNull(cookie);
        if (session == null) {
            return createNewSession(request);
        }
        return session;
    }

    public @Nullable Session getSessionOrNull(@Nullable Cookie cookie) {
        return cookie == null ? null : getSessionOrNull(cookie.value());
    }

    public @Nullable Session getSessionOrNull(@Nullable String cookieValue) {
        if (cookieValue == null) {
            return null;
        }
        try {
            long sessionId = decodeSessionId(cookieValue);
            return db.get(sessionId);
        } catch (Throwable throwable) {
            log.at(Level.WARNING).withCause(throwable).log("Failed to decode a cookie: %s".formatted(cookieValue));
            return null;
        }
    }

    public @NotNull Session createNewSession(@NotNull HttpRequestEx request) {
        long sessionId = generator.nextId();
        Session session = Session.fromRequest(sessionId, request);
        db.set(sessionId, session);
        return session;
    }

    public @NotNull String encodeSessionForCookie(@NotNull Session session) {
        return encodeSessionId(session.sessionId());
    }

    @VisibleForTesting
    @NotNull String encodeSessionId(long sessionId) {
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
            return rethrow("Failed to encrypt the data: %s".formatted(Arrays.toString(data)), e);
        }
    }

    private byte[] decryptBytes(byte[] encrypted) {
        try {
            return decipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            return rethrow("Failed to decrypt the data: %s".formatted(Arrays.toString(encrypted)), e);
        }
    }
}
