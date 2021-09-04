package io.webby.netty.marshal;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.examples.hello.JsonCustom;
import io.webby.examples.hello.SampleBean;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.*;
import static io.webby.testing.TestingBytes.asByteStream;
import static io.webby.testing.TestingBytes.asBytes;

public class JsonIntegrationTest {
    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void parse_json_simple(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = setupJson(library);

        assertJsonEquivalent(json.readBytes(asBytes("{\"foo\": 1}"), Map.class), Map.of("foo", 1));
        assertJsonEquivalent(json.readBytes(asByteStream("{\"foo\": 1}"), Map.class), Map.of("foo", 1));

        assertJsonEquivalent(json.readBytes(asBytes("{\"foo\": \"bar\"}"), Object.class), Map.of("foo", "bar"));
        assertJsonEquivalent(json.readBytes(asByteStream("{\"foo\": \"bar\"}"), Object.class), Map.of("foo", "bar"));

        assertJsonEquivalent(json.readBytes(asBytes("[]"), List.class), List.of());
        assertJsonEquivalent(json.readBytes(asByteStream("[]"), List.class), List.of());

        assertJsonEquivalent(json.readBytes(asBytes("[\"foo\"]"), List.class), List.of("foo"));
        assertJsonEquivalent(json.readBytes(asByteStream("[\"foo\"]"), List.class), List.of("foo"));

        // not working for Moshi...
        // assertJsonEquivalent(json.readBytes(asBytes("[]"), ArrayList.class), List.of());
        // assertJsonEquivalent(json.readBytes(asByteStream("[]"), ArrayList.class), List.of());
    }

    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void convert_sample_bean(MarshallerFactory.SupportedJsonLibrary library) {
        Json json = setupJson(library);
        assertJsonConversion(json, new SampleBean(123, "foo", List.of(1, 2)));
    }

    // GSON: https://github.com/google/gson/issues/1794
    @ParameterizedTest
    @EnumSource(value = MarshallerFactory.SupportedJsonLibrary.class, names = "DSL_JSON")
    public void convert_default_user(MarshallerFactory.SupportedJsonLibrary library) {
        Json json = setupJson(library);
        assertJsonConversion(json, new DefaultUser(123, UserAccess.Simple));
    }

    // @ParameterizedTest
    // @EnumSource(value = MarshallerFactory.SupportedJsonLibrary.class)
    public void convert_session(MarshallerFactory.SupportedJsonLibrary library) {
        Json json = setupJson(library);
        assertJsonConversion(json, new Session(123, -1, Instant.now(), "User-Agent", "127.0.0.1"));
    }

    private static @NotNull Json setupJson(@NotNull MarshallerFactory.SupportedJsonLibrary library) {
        return Testing.testStartup(withJsonLibrary(library), JsonCustom::customize).getInstance(Json.class);
    }
}
