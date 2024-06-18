package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import io.webby.util.time.TimeIt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static com.google.common.truth.Truth.assertThat;

public class LocalPortResolverTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Test
    public void tryResolvePort_trivial_null() {
        assertThat(tryResolvePort("jdbc")).isNull();
        assertThat(tryResolvePort("jdbc:mariadb:test")).isNull();
        assertThat(tryResolvePort("jdbc:mariadb://127:0:0:1:80/test")).isNull();
        assertThat(tryResolvePort("jdbc:mariadb://localhost/test")).isNull();
    }

    @Test
    @Tag("slow")
    public void tryResolvePort_nontrivial_null() {
        assertThat(tryResolvePort("jdbc:mariadb://host:0/test")).isNull();
    }

    @Test
    @Tag("slow")
    public void tryResolvePort_resolves() {
        assertThat(tryResolvePort("jdbc:mariadb://localhost:0/")).doesNotContain(":0");
        assertThat(tryResolvePort("jdbc:mariadb://localhost:0/test")).doesNotContain(":0");
        assertThat(tryResolvePort("jdbc:mariadb://127.0.0.1:0/test")).doesNotContain(":0");
        assertThat(tryResolvePort("jdbc:mariadb://LocalHost:0/test")).doesNotContain(":0");
    }

    private static @Nullable String tryResolvePort(@NotNull String url) {
        return TimeIt
            .timeIt(() -> LocalPortResolver.tryResolvePort(url))
            .onDone((__, millis) -> log.at(Level.INFO).log("%s -> %d ms", url, millis));
    }
}
