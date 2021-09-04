package io.webby.testing;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.webby.app.AppSettings;
import io.webby.netty.marshal.Json;
import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AssertJson {
    public static void assertJsonEquivalent(@Nullable Object actual, @NotNull Iterable<?> expected) {
        assertNotNull(actual);
        assertTrue(actual instanceof Iterable);
        BiStream.zip(Streams.stream((Iterable<?>) actual), Streams.stream(expected))
                .forEach(AssertJson::assertJsonEquivalent);
    }

    public static void assertJsonEquivalent(@Nullable Object actual, @NotNull Object expected) {
        assertNotNull(actual);
        if (expected instanceof Number number) {
            assertSameNumber(actual, number);
        } else if (expected instanceof Iterable<?> iterable) {
            assertJsonEquivalent(actual, iterable);
        } else if (expected instanceof Map<?, ?> map) {
            assertTrue(actual instanceof Map<?, ?>);
            Map<?, ?> actualMap = (Map<?, ?>) actual;
            assertEquals(map.size(), actualMap.size());
            assertJsonEquivalent(actualMap.entrySet(), map.entrySet());
        } else if (expected instanceof Map.Entry<?, ?> entry) {
            assertTrue(actual instanceof Map.Entry<?, ?>);
            Map.Entry<?, ?> actualEntry = (Map.Entry<?, ?>) actual;
            assertJsonEquivalent(actualEntry.getKey(), entry.getKey());
            assertJsonEquivalent(actualEntry.getValue(), entry.getValue());
        } else {
            assertEquals(actual, expected);
        }
    }

    public static void assertSameNumber(@Nullable Object actual, @NotNull Number expected) {
        assertNotNull(actual);
        assertTrue(actual instanceof Number);
        assertEquals(((Number) actual).intValue(), expected.intValue());
        assertEquals(((Number) actual).longValue(), expected.longValue());
        assertEquals(((Number) actual).doubleValue(), expected.doubleValue());
    }

    public static void assertJsonConversion(@NotNull Json json, @NotNull Object object) {
        String string = json.writeString(object);
        assertThat(string).isNotEmpty();
        Object another = json.readString(string, object.getClass());
        assertEquals(object, another);
    }

    public static @NotNull Consumer<AppSettings> withJsonLibrary(@NotNull SupportedJsonLibrary library) {
        return settings -> settings.setProperty("json.library", library.slug);
    }
}
