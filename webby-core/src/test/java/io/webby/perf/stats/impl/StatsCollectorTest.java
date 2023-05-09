package io.webby.perf.stats.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.assertMap;

public class StatsCollectorTest {
    private static final int KEY1 = 100_000;
    private static final int KEY2 = 200_000;
    private static final Object HINT = new Object();

    private final StatsCollector collector = new StatsCollector(777);

    @Test
    public void report_one_key_simple() {
        collector.report(KEY1, 5, 33, null);
        assertMap(collector.mainCounts()).containsExactly(KEY1, 5);
        assertMap(collector.records()).containsExactly(KEY1, List.of(recordOf(33, null)));

        collector.report(KEY1, 10, 44, HINT);
        assertMap(collector.mainCounts()).containsExactly(KEY1, 15);
        assertMap(collector.records()).containsExactly(KEY1, List.of(recordOf(33, null), recordOf(44, HINT)));

        collector.report(KEY1, 0, 11, null);
        assertMap(collector.mainCounts()).containsExactly(KEY1, 15);
        assertMap(collector.records())
            .containsExactly(KEY1, List.of(recordOf(33, null), recordOf(44, HINT), recordOf(11, null)));
    }

    @Test
    public void report_two_keys_simple() {
        collector.report(KEY1, 5, 66, null);
        collector.report(KEY2, 8, 77, HINT);

        assertMap(collector.mainCounts()).containsExactly(KEY1, 5, KEY2, 8);
        assertMap(collector.records()).containsExactly(KEY1, List.of(recordOf(66, null)), KEY2, List.of(recordOf(77, HINT)));
    }

    @Test
    public void lock_unlock_simple() {
        boolean locked = collector.lock();
        assertThat(locked).isTrue();
        long millis = collector.unlock();
        assertThat(millis).isAtLeast(0);
    }

    @Test
    public void lock_lock_simple() {
        boolean locked = collector.lock();
        assertThat(locked).isTrue();
        boolean lockedAgain = collector.lock();
        assertThat(lockedAgain).isFalse();
    }

    @Test
    public void unlock_simple() {
        long millis = collector.unlock();
        assertThat(millis).isEqualTo(0);
    }

    @Test
    public void lock_unlock_report_simple() {
        boolean locked = collector.lock();
        assertThat(locked).isTrue();

        collector.unlockAndReport(KEY1, 3, HINT);
        assertMap(collector.mainCounts()).containsExactly(KEY1, 3);
        assertMap(collector.records()).asJavaMap().containsKey(KEY1);
    }

    @Test
    public void stopwatch_simple() {
        assertThat(collector.isActive()).isTrue();
        assertThat(collector.totalElapsed(TimeUnit.NANOSECONDS)).isAtLeast(0);

        collector.stop();
        assertThat(collector.isActive()).isFalse();
        assertThat(collector.totalElapsed(TimeUnit.NANOSECONDS)).isAtLeast(0);

        collector.stop();
        assertThat(collector.isActive()).isFalse();
        assertThat(collector.totalElapsed(TimeUnit.NANOSECONDS)).isAtLeast(0);
    }

    private static @NotNull StatsRecord recordOf(int elapsedMillis, @Nullable Object hint) {
        return new StatsRecord(elapsedMillis, hint);
    }
}
