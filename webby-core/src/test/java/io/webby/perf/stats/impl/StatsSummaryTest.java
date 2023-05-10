package io.webby.perf.stats.impl;

import com.google.gson.Gson;
import io.webby.perf.stats.Stat;
import io.webby.testing.Testing;
import io.webby.testing.ext.HppcIterationSeedExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

public class StatsSummaryTest {
    @RegisterExtension static final HppcIterationSeedExtension ITERATION_SEED = new HppcIterationSeedExtension();

    private final StatsCollector stats = new StatsCollector(999);
    private final StatsSummary summary = new StatsSummary(Testing.defaultAppSettings(), stats);

    @Test
    public void summary_no_reports() {
        long millis = stats.stop().totalElapsed(TimeUnit.MILLISECONDS);

        assertThat(summary.mainAsJson()).isEqualTo("{time:%d}".formatted(millis));
        assertThat(summary.mainAsTable()).isEqualTo("""
            Total       | % 4d ms |
            """.formatted(millis).trim());
        assertThat(summary.recordsAsJson()).isEqualTo("{}");
    }

    @Test
    public void summary_simple() {
        stats.report(Stat.RENDER.key(), 123, 11, null);
        stats.report(Stat.DB_IO.key(), 456, 22, null);
        long millis = stats.stop().totalElapsed(TimeUnit.MILLISECONDS);

        assertThat(summary.mainAsJson()).isEqualTo("{render:123,db_io:456,time:%d}".formatted(millis));
        assertThat(summary.mainAsTable()).isEqualTo("""
            Total       | % 4d ms |
            db_io       |   22 ms |  456 calls
            render      |   11 ms |  123 bytes
            """.formatted(millis).trim());
        assertThat(summary.recordsAsJson()).isEqualTo("{'db_io':[[22]],'render':[[11]]}");
    }

    @Test
    public void summary_long_numbers() {
        stats.report(Stat.CODEC_WRITE.key(), 123456, 1111, null);
        stats.report(Stat.CODEC_READ.key(), 654321, 2222, null);
        long millis = stats.stop().totalElapsed(TimeUnit.MILLISECONDS);

        assertThat(summary.mainAsJson()).isEqualTo("{codec_write:123456,codec_read:654321,time:%d}".formatted(millis));
        assertThat(summary.mainAsTable()).isEqualTo("""
            Total       | % 4d ms |
            codec_read  | 2222 ms | 654321 bytes
            codec_write | 1111 ms | 123456 bytes
            """.formatted(millis).trim());
        assertThat(summary.recordsAsJson()).isEqualTo("{'codec_read':[[2222]],'codec_write':[[1111]]}");
    }

    @Test
    public void summary_single_key_reported_twice() {
        stats.report(Stat.RENDER.key(), 499, 111, "abc");
        stats.report(Stat.RENDER.key(), 500, 222, null);
        long millis = stats.stop().totalElapsed(TimeUnit.MILLISECONDS);

        assertThat(summary.mainAsJson()).isEqualTo("{render:999,time:%d}".formatted(millis));
        assertThat(summary.mainAsTable()).isEqualTo("""
            Total       | % 4d ms |
            render      |  333 ms |  999 bytes
            """.formatted(millis).trim());
        assertThat(summary.recordsAsJson()).isEqualTo("{'render':[[111,'abc'],[222]]}");
    }

    @Test
    public void summary_escape_json() {
        String hint = "\\ '\"'";
        stats.report(Stat.RENDER.key(), 123, 111, hint);
        long millis = stats.stop().totalElapsed(TimeUnit.MILLISECONDS);

        assertThat(summary.mainAsJson()).isEqualTo("{render:123,time:%d}".formatted(millis));
        assertThat(summary.mainAsTable()).isEqualTo("""
            Total       | % 4d ms |
            render      |  111 ms |  123 bytes
            """.formatted(millis).trim());
        assertThat(summary.recordsAsJson()).isEqualTo("{'render':[[111,'\\\\ \\'\\\"\\'']]}");

        record Records(String[][] render) {}
        Records parsed = parseJson(summary.recordsAsJson(), Records.class);
        assertThat(parsed.render()[0]).asList().containsExactly("111", hint);
    }

    private static <T> T parseJson(@NotNull String str, @NotNull Class<T> klass) {
        return new Gson().fromJson(str, klass);
    }
}
