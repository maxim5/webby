package io.spbx.webby.netty.response;

import com.google.inject.Inject;
import io.spbx.util.base.Pair;
import io.spbx.webby.app.Settings;
import io.spbx.webby.netty.HttpConst;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResponseHeaders {
    @Inject private Settings settings;
    @Inject private Charset charset;

    public @NotNull CharSequence ensureCharset(@NotNull CharSequence contentType) {
        boolean isUtf8 = charset == StandardCharsets.UTF_8;
        if (contentType == HttpConst.TEXT_HTML) {
            return isUtf8 ? HttpConst.TEXT_HTML_UTF8 : "%s; charset=%s".formatted(contentType, charset);
        }
        if (contentType == HttpConst.TEXT_PLAIN) {
            return isUtf8 ? HttpConst.TEXT_PLAIN_UTF8 : "%s; charset=%s".formatted(contentType, charset);
        }
        return contentType;
    }

    public @NotNull List<Pair<CharSequence, CharSequence>> defaultHeaders(@NotNull CharSequence contentType) {
        if (settings.isDevMode()) {
            return List.of(
                    Pair.of(HttpConst.CONTENT_TYPE, ensureCharset(contentType))
            );
        }
        return List.of(
                Pair.of(HttpConst.CONTENT_TYPE, ensureCharset(contentType)),
                Pair.of(HttpConst.X_FRAME_OPTIONS, HttpConst.SAMEORIGIN),               // iframe attack
                Pair.of(HttpConst.X_XSS_PROTECTION, HttpConst.MODE_BLOCK),              // XSS Filtering
                Pair.of(HttpConst.CONTENT_SECURITY_POLICY, HttpConst.SCRIPT_SRC_SELF),  // Whitelisting Sources
                Pair.of(HttpConst.X_CONTENT_TYPE_OPTIONS, HttpConst.NOSNIFF)            // MIME-sniffing attacks

                // Content-Security-Policy: upgrade-insecure-requests
                // Strict-Transport-Security: max-age=1000; includeSubDomains; preload
        );
    }
}
