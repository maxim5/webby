package io.webby.perf.stats.impl;

import com.google.common.collect.Streams;
import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.netty.HttpConst;
import io.webby.perf.stats.Stat;
import io.webby.util.LazyBoolean;
import io.webby.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StatsSummary {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final RequestStatsCollector stats;
    private final LazyBoolean isRecordsSummaryEnabled;
    private final Level logLevel;

    public StatsSummary(@NotNull Settings settings, @NotNull RequestStatsCollector stats) {
        this.stats = stats.stop();
        this.isRecordsSummaryEnabled = new LazyBoolean(() ->
            settings.getBoolProperty("perf.track.summary.records.enabled", false)
        );
        this.logLevel = settings.isDevMode() ? Level.INFO : Level.FINE;
    }

    public void summarize(@NotNull HttpResponse response) {
        if (log.at(logLevel).isEnabled()) {
            log.at(logLevel).log("Performance stats summary for %s:\n%s\n", stats.uri(), mainAsTable());
        }

        // See https://stackoverflow.com/questions/220231/accessing-the-web-pages-http-headers-in-javascript
        String timing = isRecordsSummaryEnabled.get() ?
                "%s;desc=\"%s\", %s;desc=\"%s\"".formatted("main", mainAsJson(), "records", recordsAsJson()) :
                "%s;desc=\"%s\"".formatted("main", mainAsJson());
        response.headers().add(HttpConst.SERVER_TIMING, timing);
    }

    private @NotNull String mainAsTable() {
        String ROW_FMT = "%-" + Stat.MAX_NAME_LENGTH + "s | %4d ms | %4s %s";

        StringBuilder builder = new StringBuilder((stats.main().size() + 1) * 36);
        builder.append(ROW_FMT.formatted("Total", stats.totalElapsed(TimeUnit.MILLISECONDS), "", ""));
        Formatter formatter = new Formatter(builder);
        stats.forEach((key, value, records) -> {
            builder.append('\n');
            long totalMillis = records.stream().mapToLong(StatsRecord::elapsedMillis).sum();
            formatter.format(ROW_FMT, key.lowerName(), totalMillis, value, key.unit().lowerName());
        });
        return builder.toString();
    }

    private @NotNull String mainAsJson() {
        List<Pair<String, Long>> pairs = Streams.stream(stats.main())
                .map(cursor -> Pair.of(Stat.NAMES.get(cursor.key), (long) cursor.value))
                .collect(Collectors.toCollection(ArrayList::new));
        pairs.add(Pair.of("time", stats.totalElapsed(TimeUnit.MILLISECONDS)));
        return pairs.stream()
                .map(pair -> "%s:%d".formatted(pair.first(), pair.second()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private @NotNull String recordsAsJson() {
        return Streams.stream(stats.records())
                .map(cursor -> {
                    String name = Stat.NAMES.get(cursor.key);
                    String value = cursor.value.stream()
                            .map(StatsRecord::toCompactString)
                            .collect(Collectors.joining(",", "[", "]"));
                    return "'%s':%s".formatted(name, value);
                }).collect(Collectors.joining(",", "{", "}"));
    }
}
