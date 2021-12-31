package io.webby.netty.marshal;

import io.webby.testing.TestingModels;
import io.webby.demo.hello.JsonCustom;
import io.webby.demo.hello.SampleBean;
import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.webby.testing.AssertJson.*;
import static io.webby.testing.TestingBytes.*;
import static io.webby.url.view.EasyRender.outputToBytes;
import static io.webby.url.view.EasyRender.writeToString;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class JsonIntegrationTest {
    public JsonIntegrationTest(@NotNull SupportedJsonLibrary library) {
        Testing.testStartup(withJsonLibrary(library), JsonCustom::customize);
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static SupportedJsonLibrary[] libraries() {
        return SupportedJsonLibrary.values();
    }
    
    @Test
    public void parse_json_to_map() throws Exception {
        // Map interface
        assertJsonRead("{}", Map.class, Map.of());
        assertJsonRead("{\"foo\": 1}", Map.class, Map.of("foo", 1));
        assertJsonRead("{\"foo\": \"bar\"}", Map.class, Map.of("foo", "bar"));
        assertJsonRead("{\"foo\": null}", Map.class, Collections.singletonMap("foo", null));

        // Concrete Map types
        assertJsonRead("{}", HashMap.class, Map.of());
        assertJsonRead("{\"foo\": \"bar\"}", HashMap.class, Map.of("foo", "bar"));
        assertJsonRead("{}", LinkedHashMap.class, Map.of());
        assertJsonRead("{\"foo\": \"bar\"}", LinkedHashMap.class, Map.of("foo", "bar"));
        assertJsonRead("{}", ConcurrentHashMap.class, Map.of());
        assertJsonRead("{\"foo\": \"bar\"}", ConcurrentHashMap.class, Map.of("foo", "bar"));

        // Map should be a default for Object
        assertJsonRead("{\"foo\": \"bar\"}", Object.class, Map.of("foo", "bar"));
    }

    @Test
    public void parse_json_to_list() throws Exception {
        // List interface
        assertJsonRead("[]", List.class, List.of());
        assertJsonRead("[\"foo\"]", List.class, List.of("foo"));
        assertJsonRead("[123]", List.class, List.of(123));
        assertJsonRead("[123, \"foo\", null]", List.class, Arrays.asList(123, "foo", null));

        // Concrete List types
        assertJsonRead("[]", ArrayList.class, List.of());
        assertJsonRead("[\"foo\"]", ArrayList.class, List.of("foo"));
        assertJsonRead("[]", LinkedList.class, List.of());
        assertJsonRead("[0]", LinkedList.class, List.of(0));

        // Other collections
        assertJsonRead("[]", Set.class, List.of());
        assertJsonRead("[0]", Set.class, List.of(0));
        assertJsonRead("[]", HashSet.class, List.of());
        assertJsonRead("[1]", HashSet.class, List.of(1));
        assertJsonRead("[]", ArrayDeque.class, List.of());
        assertJsonRead("[2]", ArrayDeque.class, List.of(2));
    }

    @Test
    public void to_json_from_map() throws Exception {
        // Map interface
        assertJsonWrite(Map.of(), "{}");
        assertJsonWrite(Map.of("foo", 1), "{\"foo\":1}");
        assertJsonWrite(Map.of("foo", "bar"), "{\"foo\":\"bar\"}");

        // Concrete Map types
        assertJsonWrite(new HashMap<>(), "{}");
        assertJsonWrite(new HashMap<>(Map.of("foo", 1)), "{\"foo\":1}");
        assertJsonWrite(new LinkedHashMap<>(), "{}");
        assertJsonWrite(new LinkedHashMap<>(Map.of("foo", 1)), "{\"foo\":1}");
        assertJsonWrite(new ConcurrentHashMap<>(), "{}");
        assertJsonWrite(new ConcurrentHashMap<>(Map.of("foo", 1)), "{\"foo\":1}");

        // assertJsonWrite(new Object(), "{}");
    }

    @Test
    public void to_json_from_list() throws Exception {
        // List interface
        assertJsonWrite(List.of(), "[]");
        assertJsonWrite(List.of("foo"), "[\"foo\"]");
        assertJsonWrite(List.of(1, 2, 3), "[1,2,3]");

        // Concrete List types
        assertJsonWrite(Collections.emptyList(), "[]");
        assertJsonWrite(Arrays.asList(123, "foo", null), "[123,\"foo\",null]");
        assertJsonWrite(new ArrayList<>(), "[]");
        assertJsonWrite(new ArrayList<>(List.of("foo")), "[\"foo\"]");
        assertJsonWrite(new LinkedList<>(), "[]");
        assertJsonWrite(new LinkedList<>(List.of("foo")), "[\"foo\"]");

        // Other collections
        assertJsonWrite(new HashSet<>(), "[]");
        assertJsonWrite(new HashSet<>(List.of(0)), "[0]");
        assertJsonWrite(new ArrayDeque<>(), "[]");
        assertJsonWrite(new ArrayDeque<>(List.of(1)), "[1]");
    }

    @Test
    public void roundtrip_sample_bean() {
        assertJsonStringRoundTrip(new SampleBean(123, "foo", List.of(1, 2)));
    }

    // GSON: https://github.com/google/gson/issues/1794
    // @Test
    public void roundtrip_default_user() {
        assumeTrue(getJsonLibrary() == SupportedJsonLibrary.DSL_JSON);
        assertJsonStringRoundTrip(TestingModels.newUserNow(123));
    }

    // @ParameterizedTest
    // @EnumSource(value = MarshallerFactory.SupportedJsonLibrary.class)
    public void roundtrip_session() {
        assertJsonStringRoundTrip(TestingModels.newSessionNow());
    }

    private static void assertJsonRead(String input, Class<?> klass, Object expected) throws Exception {
        Json json = Testing.Internals.json();
        assertJsonEquivalent(json.readString(input, klass), expected);
        assertJsonEquivalent(json.readChars(new StringReader(input), klass), expected);
        assertJsonEquivalent(json.readBytes(asBytes(input), klass), expected);
        assertJsonEquivalent(json.readBytes(asByteStream(input), klass), expected);
        assertJsonEquivalent(json.readByteBuf(asByteBuf(input), klass), expected);
    }

    private static void assertJsonWrite(Object instance, String expected) throws Exception {
        Json json = Testing.Internals.json();
        assertEquals(expected, json.writeString(instance));
        assertEquals(expected, writeToString(writer -> json.writeChars(writer, instance)));
        assertBytes(json.writeBytes(instance), expected);
        assertBytes(outputToBytes(outputStream -> json.writeBytes(outputStream, instance)), expected);
        assertByteBuf(json.writeByteBuf(instance), expected);
    }
}
