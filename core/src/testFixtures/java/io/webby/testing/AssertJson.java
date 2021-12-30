package io.webby.testing;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.netty.buffer.ByteBuf;
import io.webby.app.AppSettings;
import io.webby.netty.marshal.Json;
import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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

    public static void assertJsonEquivalent(@Nullable Object actual, @Nullable Object expected) {
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

    public static void assertJsonStringRoundTrip(@NotNull Object object) {
        Json json = Testing.Internals.json();
        String string = json.writeString(object);
        assertThat(string).isNotEmpty();
        Object another = json.readString(string, object.getClass());
        assertEquals(object, another);
    }

    public static void assertJsonValue(@NotNull ByteBuf actual, @NotNull Object object) {
        Json json = Testing.Internals.json();
        Object another = json.readByteBuf(actual, object.getClass());
        assertEquals(object, another);
    }

    public static @NotNull Consumer<AppSettings> withJsonLibrary(@NotNull SupportedJsonLibrary library) {
        return settings -> settings.setProperty("json.library", library.slug);
    }

    public static @NotNull SupportedJsonLibrary getJsonLibrary() {
        String property = Testing.Internals.settings().getProperty("json.library");
        return Arrays.stream(SupportedJsonLibrary.values()).filter(val -> val.slug.equals(property)).findAny().orElseThrow();
    }
}
