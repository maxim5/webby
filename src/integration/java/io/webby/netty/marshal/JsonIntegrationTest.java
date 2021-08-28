package io.webby.netty.marshal;

import io.webby.examples.hello.JsonCustom;
import io.webby.testing.Testing;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static io.webby.testing.AssertJson.withJsonLibrary;
import static io.webby.testing.TestingBytes.asByteStream;
import static io.webby.testing.TestingBytes.asBytes;

public class JsonIntegrationTest {
    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void parse_json_simple(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = Testing.testStartup(withJsonLibrary(library), JsonCustom::customize).getInstance(Json.class);

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
}
