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

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.webby.testing.AssertJson.*;
import static io.webby.testing.TestingBytes.*;
import static io.webby.url.view.EasyRender.outputToBytes;
import static io.webby.url.view.EasyRender.writeToString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonIntegrationTest {
    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void parse_json_to_map(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = setupJson(library);

        // Map interface
        assertJsonRead(json, "{}", Map.class, Map.of());
        assertJsonRead(json, "{\"foo\": 1}", Map.class, Map.of("foo", 1));
        assertJsonRead(json, "{\"foo\": \"bar\"}", Map.class, Map.of("foo", "bar"));
        assertJsonRead(json, "{\"foo\": null}", Map.class, Collections.singletonMap("foo", null));

        // Concrete Map types
        assertJsonRead(json, "{\"foo\": \"bar\"}", HashMap.class, Map.of("foo", "bar"));
        assertJsonRead(json, "{\"foo\": \"bar\"}", LinkedHashMap.class, Map.of("foo", "bar"));
        assertJsonRead(json, "{\"foo\": \"bar\"}", ConcurrentHashMap.class, Map.of("foo", "bar"));

        // Map should be a default for Object
        assertJsonRead(json, "{\"foo\": \"bar\"}", Object.class, Map.of("foo", "bar"));
    }

    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void parse_json_to_list(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = setupJson(library);

        // List interface
        assertJsonRead(json, "[]", List.class, List.of());
        assertJsonRead(json, "[\"foo\"]", List.class, List.of("foo"));
        assertJsonRead(json, "[123]", List.class, List.of(123));
        assertJsonRead(json, "[123, \"foo\", null]", List.class, Arrays.asList(123, "foo", null));

        // Concrete List types
        assertJsonRead(json, "[]", ArrayList.class, List.of());
        assertJsonRead(json, "[0]", LinkedList.class, List.of(0));

        // Other collections
        assertJsonRead(json, "[]", Set.class, List.of());
        assertJsonRead(json, "[1]", HashSet.class, List.of(1));
        assertJsonRead(json, "[2]", ArrayDeque.class, List.of(2));
    }

    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void to_json_from_map(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = setupJson(library);

        // Map interface
        assertJsonWrite(json, Map.of(), "{}");
        assertJsonWrite(json, Map.of("foo", 1), "{\"foo\":1}");
        assertJsonWrite(json, Map.of("foo", "bar"), "{\"foo\":\"bar\"}");

        // Concrete Map types
        assertJsonWrite(json, new HashMap<>(), "{}");
        assertJsonWrite(json, new LinkedHashMap<>(), "{}");
        assertJsonWrite(json, new ConcurrentHashMap<>(), "{}");

        // assertJsonWrite(json, new Object(), "{}");
    }

    @ParameterizedTest
    @EnumSource(MarshallerFactory.SupportedJsonLibrary.class)
    public void to_json_from_list(MarshallerFactory.SupportedJsonLibrary library) throws Exception {
        Json json = setupJson(library);

        // List interface
        assertJsonWrite(json, List.of(), "[]");
        assertJsonWrite(json, List.of("foo"), "[\"foo\"]");
        assertJsonWrite(json, List.of(1, 2, 3), "[1,2,3]");

        // Concrete List types
        assertJsonWrite(json, Arrays.asList(123, "foo", null), "[123,\"foo\",null]");
        assertJsonWrite(json, new ArrayList<>(), "[]");
        assertJsonWrite(json, new LinkedList<>(List.of("foo")), "[\"foo\"]");

        // Other collections
        assertJsonWrite(json, new HashSet<>(List.of(0)), "[0]");
        assertJsonWrite(json, new ArrayDeque<>(List.of(1)), "[1]");
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

    private static void assertJsonRead(Json json, String input, Class<?> klass, Object expected) throws Exception {
        assertJsonEquivalent(json.readString(input, klass), expected);
        assertJsonEquivalent(json.readChars(new StringReader(input), klass), expected);
        assertJsonEquivalent(json.readBytes(asBytes(input), klass), expected);
        assertJsonEquivalent(json.readBytes(asByteStream(input), klass), expected);
        assertJsonEquivalent(json.readByteBuf(asByteBuf(input), klass), expected);
    }

    private void assertJsonWrite(Json json, Object instance, String expected) throws IOException {
        assertEquals(expected, json.writeString(instance));
        assertEquals(expected, writeToString(writer -> json.writeChars(writer, instance)));
        assertBytes(json.writeBytes(instance), expected);
        assertBytes(outputToBytes(outputStream -> json.writeBytes(outputStream, instance)), expected);
        assertByteBuf(json.writeByteBuf(instance), expected);
    }

    private static @NotNull Json setupJson(@NotNull MarshallerFactory.SupportedJsonLibrary library) {
        return Testing.testStartup(withJsonLibrary(library), JsonCustom::customize).getInstance(Json.class);
    }
}
