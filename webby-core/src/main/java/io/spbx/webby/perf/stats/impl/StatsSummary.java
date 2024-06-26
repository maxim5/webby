package io.spbx.webby.perf.stats.impl;

import com.google.common.collect.Streams;
import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.spbx.util.base.Pair;
import io.spbx.util.lazy.LazyBoolean;
import io.spbx.webby.app.Settings;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.perf.stats.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StatsSummary {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final StatsCollector stats;
    private final LazyBoolean isRecordsSummaryEnabled;
    private final Level logLevel;

    public StatsSummary(@NotNull Settings settings, @NotNull StatsCollector stats) {
        this.stats = stats;
        this.isRecordsSummaryEnabled = new LazyBoolean(() ->
            settings.getBool("perf.track.summary.records.enabled", false)
        );
        this.logLevel = settings.isDevMode() ? Level.INFO : Level.FINE;
    }

    public void summarizeFor(@NotNull HttpRequest request, @NotNull HttpResponse response) {
        if (log.at(logLevel).isEnabled()) {
            log.at(logLevel).log("Performance stats summary for %s:\n%s\n", request.uri(), mainAsTable());
        }

        // See https://stackoverflow.com/questions/220231/accessing-the-web-pages-http-headers-in-javascript
        String timing = isRecordsSummaryEnabled.get() ?
            "%s;desc=\"%s\", %s;desc=\"%s\"".formatted("main", mainAsJson(), "records", recordsAsJson()) :
            "%s;desc=\"%s\"".formatted("main", mainAsJson());
        response.headers().add(HttpConst.SERVER_TIMING, timing);
    }

    @VisibleForTesting
    @NotNull String mainAsTable() {
        String ROW_FMT = "%-" + Stat.index().maxNameLength() + "s | %4d ms | %4s %s";

        StringBuilder builder = new StringBuilder((stats.mainCounts().size() + 1) * 36);
        builder.append(ROW_FMT.formatted("Total", stats.totalElapsed(TimeUnit.MILLISECONDS), "", "").trim());
        Formatter formatter = new Formatter(builder);
        stats.forEach((key, value, records) -> {
            builder.append('\n');
            long totalMillis = records.stream().mapToLong(StatsRecord::elapsedMillis).sum();
            formatter.format(ROW_FMT, key.name(), totalMillis, value, key.unit().lowerName());
        });
        return builder.toString();
    }

    @VisibleForTesting
    @NotNull String mainAsJson() {
        List<Pair<String, Long>> pairs = Streams.stream(stats.mainCounts())
            .map(cursor -> Pair.of(Stat.index().findNameOrDummy(cursor.key), (long) cursor.value))
            .collect(Collectors.toCollection(ArrayList::new));
        pairs.add(Pair.of("time", stats.totalElapsed(TimeUnit.MILLISECONDS)));
        return pairs.stream()
            .map(pair -> pair.mapFirst(StatsSummary::simpleEscapeJson))
            .map(pair -> "%s:%d".formatted(pair.first(), pair.second()))
            .collect(Collectors.joining(",", "{", "}"));
    }

    @VisibleForTesting
    @NotNull String recordsAsJson() {
        return Streams.stream(stats.records())
            .map(cursor -> {
                String name = Stat.index().findNameOrDummy(cursor.key);
                String value = cursor.value.stream()
                    .map(StatsSummary::toCompactJsonString)
                    .collect(Collectors.joining(",", "[", "]"));
                return "'%s':%s".formatted(name, value);
            }).collect(Collectors.joining(",", "{", "}"));
    }

    private static @NotNull String toCompactJsonString(@NotNull StatsRecord record) {
        return record.hint() != null ?
            "[%d,'%s']".formatted(record.elapsedMillis(), simpleEscapeJson(record.hint().toString())) :
            "[%d]".formatted(record.elapsedMillis());
    }

    private static @NotNull String simpleEscapeJson(@NotNull String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    }
}
