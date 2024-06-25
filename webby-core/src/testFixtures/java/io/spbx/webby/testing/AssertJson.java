package io.spbx.webby.testing;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.netty.buffer.ByteBuf;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.netty.marshal.Json;
import io.spbx.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;

public class AssertJson {
    public static void assertJsonEquivalent(@Nullable Object actual, @NotNull Iterable<?> expected) {
        assertThat(actual).isNotNull();
        assertThat(actual instanceof Iterable).isTrue();
        BiStream.zip(Streams.stream((Iterable<?>) actual), Streams.stream(expected))
            .forEach(AssertJson::assertJsonEquivalent);
    }

    public static void assertJsonEquivalent(@Nullable Object actual, @Nullable Object expected) {
        if (expected instanceof Number number) {
            assertSameNumber(actual, number);
        } else if (expected instanceof Iterable<?> iterable) {
            assertJsonEquivalent(actual, iterable);
        } else if (expected instanceof Map<?, ?> map) {
            assertThat(actual instanceof Map<?, ?>).isTrue();
            Map<?, ?> actualMap = (Map<?, ?>) actual;
            assertThat(actualMap.size()).isEqualTo(map.size());
            assertJsonEquivalent(actualMap.entrySet(), map.entrySet());
        } else if (expected instanceof Map.Entry<?, ?> entry) {
            assertThat(actual instanceof Map.Entry<?, ?>).isTrue();
            Map.Entry<?, ?> actualEntry = (Map.Entry<?, ?>) actual;
            assertJsonEquivalent(actualEntry.getKey(), entry.getKey());
            assertJsonEquivalent(actualEntry.getValue(), entry.getValue());
        } else {
            assertThat(expected).isEqualTo(actual);
        }
    }

    public static void assertSameNumber(@Nullable Object actual, @NotNull Number expected) {
        assertThat(actual).isNotNull();
        assertThat(actual instanceof Number).isTrue();
        assertThat(expected.intValue()).isEqualTo(((Number) actual).intValue());
        assertThat(expected.longValue()).isEqualTo(((Number) actual).longValue());
        assertThat(expected.doubleValue()).isEqualTo(((Number) actual).doubleValue());
    }

    public static void assertJsonStringRoundTrip(@NotNull Object object) {
        Json json = Testing.Internals.json();
        String string = json.writeString(object);
        assertThat(string).isNotEmpty();
        Object another = json.readString(string, object.getClass());
        assertThat(another).isEqualTo(object);
    }

    public static void assertJsonValue(@NotNull ByteBuf actual, @NotNull Object object) {
        Json json = Testing.Internals.json();
        Object another = json.readByteBuf(actual, object.getClass());
        assertThat(another).isEqualTo(object);
    }

    public static @NotNull Consumer<AppSettings> withJsonLibrary(@NotNull SupportedJsonLibrary library) {
        return settings -> settings.setProperty("json.library", library.slug);
    }

    public static @NotNull SupportedJsonLibrary getJsonLibrary() {
        String property = Testing.Internals.settings().getOrNull("json.library");
        return Arrays.stream(SupportedJsonLibrary.values()).filter(val -> val.slug.equals(property)).findAny().orElseThrow();
    }
}
