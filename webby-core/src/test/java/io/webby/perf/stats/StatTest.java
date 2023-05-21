package io.webby.perf.stats;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatTest {
    @Test
    public void default_stats() {
        assertStatInIndex(Stat.RENDER);
        assertStatInIndex(Stat.DB_GET);
        assertStatInIndex(Stat.DB_SET);
        assertStatInIndex(Stat.CODEC_READ);
        assertStatInIndex(Stat.CODEC_WRITE);
    }

    @Test
    public void register_duplicate_key() {
        assertThrows(AssertionError.class, () -> Stat.registerStat("foo", 1, Stat.Unit.NONE));
    }

    @Test
    public void unregistered_stat() {
        Stat unregistered = new Stat("unregistered", 888, Stat.Unit.BYTES);

        assertThat(Stat.index().findStatOrNull(unregistered.key())).isNull();
        assertThat(Stat.index().findNameOrNull(unregistered.key())).isNull();
        assertThat(Stat.index().findStatOrDummy(unregistered.key())).isEqualTo(new Stat("888", 888, Stat.Unit.NONE));
        assertThat(Stat.index().findNameOrDummy(unregistered.key())).isEqualTo("888");
    }

    @Test
    public void custom_stat() {
        Stat custom = Stat.registerStat("custom", 777, Stat.Unit.NONE);
        try {
            assertStatInIndex(custom);
        } finally {
            Stat.unregisterStat(custom);
        }
    }

    private static void assertStatInIndex(@NotNull Stat stat) {
        assertThat(Stat.index().findStatOrNull(stat.key())).isEqualTo(stat);
        assertThat(Stat.index().findNameOrNull(stat.key())).isEqualTo(stat.name());
    }
}
