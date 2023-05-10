package io.webby.testing;

import com.google.common.collect.Streams;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.HttpConst;
import io.webby.perf.stats.Stat;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AssertStats {
    public static void assertNoStatsHeaders(@NotNull HttpResponse response) {
        assertNull(response.headers().get(HttpConst.SERVER_TIMING));
    }

    public static void assertStatsHeader(@NotNull HttpResponse response, @NotNull Stat... expectedStats) {
        assertStatsHeader(response, Arrays.stream(expectedStats).toList());
    }

    public static void assertStatsHeader(@NotNull HttpResponse response, @NotNull Iterable<Stat> expectedStats) {
        String serverTiming = response.headers().get(HttpConst.SERVER_TIMING);
        assertNotNull(serverTiming);

        Matcher matcher = Pattern.compile("main;desc=\"\\{(.*)}\"").matcher(serverTiming);
        assertTrue(matcher.matches());

        Set<String> keys = Arrays.stream(matcher.group(1).split(","))
            .map(part -> part.split(":")[0])
            .collect(Collectors.toSet());
        Set<String> expected = Stream.concat(
            Streams.stream(expectedStats).map(stat -> stat.name().toLowerCase()),
            Stream.of("time")
        ).collect(Collectors.toSet());
        assertEquals(expected, keys);
    }
}
