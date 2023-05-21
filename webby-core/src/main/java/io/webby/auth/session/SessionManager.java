package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.app.Settings;
import io.webby.auth.user.UserModel;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

import static io.webby.util.base.Unchecked.rethrow;

public class SessionManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Cipher cipher;
    private final Cipher decipher;
    private final SessionStore store;
    private final Factory factory;

    @Inject
    public SessionManager(@NotNull Settings settings,
                          @NotNull SessionStore store,
                          @NotNull SessionManager.Factory factory) throws Exception {
        cipher = Cipher.getInstance("AES");
        decipher = Cipher.getInstance("AES");

        Key key = new SecretKeySpec(settings.securityKey(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        decipher.init(Cipher.DECRYPT_MODE, key);

        this.store = store;
        this.factory = factory;
    }

    public @NotNull SessionModel getOrCreateSession(@NotNull HttpRequestEx request, @Nullable Cookie cookie) {
        SessionModel session = getSessionOrNull(cookie);
        if (session == null) {
            return createNewSession(request);
        }
        return session;
    }

    public @Nullable SessionModel getSessionOrNull(@Nullable Cookie cookie) {
        return cookie == null ? null : getSessionOrNull(cookie.value());
    }

    public @Nullable SessionModel getSessionOrNull(@Nullable String cookieValue) {
        if (cookieValue == null) {
            return null;
        }
        try {
            long sessionId = decodeSessionId(cookieValue);
            return store.getSessionByIdOrNull(sessionId);
        } catch (Throwable throwable) {
            log.at(Level.WARNING).withCause(throwable).log("Failed to decode a cookie: %s", cookieValue);
            return null;
        }
    }

    public @NotNull SessionModel createNewSession(@NotNull HttpRequestEx request) {
        SessionData data = factory.newSessionData(request);
        return store.createSessionAutoId(data);
    }

    public @NotNull SessionModel addUserOrDie(@NotNull SessionModel session, @NotNull UserModel user) {
        assert !session.hasUserId() : "Session already has a user: session=%s user=%s".formatted(session, user);
        SessionModel newSession = session.withUser(user);
        store.updateSessionById(newSession);
        return newSession;
    }

    public @NotNull String encodeSessionForCookie(@NotNull SessionModel session) {
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

    public interface Factory {
        @NotNull SessionData newSessionData(@NotNull HttpRequestEx request);
    }
}
