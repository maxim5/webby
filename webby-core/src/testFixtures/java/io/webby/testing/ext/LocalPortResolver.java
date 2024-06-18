package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import io.webby.db.sql.SqlSettings;
import io.spbx.util.io.EasyNet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.logging.Level;

class LocalPortResolver {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static @NotNull SqlSettings tryResolveSqlSettings(@NotNull SqlSettings settings) {
        String sourceUrl = settings.url();
        String resolvedUrl = tryResolvePort(sourceUrl);
        return resolvedUrl != null ? settings.withUrl(resolvedUrl) : settings;
    }

    public static @Nullable String tryResolvePort(@NotNull String url) {
        URI uri = SqlSettings.parseJdbcUrl(url);
        if (uri == null) { return null; }
        String host = uri.getHost();
        int port = uri.getPort();

        if (host != null && port == 0 && EasyNet.isKnownLocalhost(host)) {
            int availablePort = EasyNet.nextAvailablePort();
            log.at(Level.FINE).log("[SQL] Resolved local port: %d", availablePort);

            String curHostAndPort = "%s:%d".formatted(host, port);
            String newHostAndPort = "%s:%d".formatted(host, availablePort);
            if (containsOnce(url, curHostAndPort)) {
                return url.replace(curHostAndPort, newHostAndPort);
            } else {
                log.at(Level.WARNING).log("[SQL] Expected to find host/port (%s) in the url, but not found: %s",
                                          curHostAndPort, url);
            }
        }

        return null;
    }

    private static boolean containsOnce(@NotNull String big, String small) {
        int firstIndex = big.indexOf(small);
        return firstIndex >= 0 && firstIndex == big.lastIndexOf(small);
    }
}
