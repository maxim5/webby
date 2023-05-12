package io.webby.demo.hello;

import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static io.webby.testing.AssertJson.withJsonLibrary;
import static io.webby.testing.AssertResponse.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(Parameterized.class)
public class AcceptJsonContentIntegrationTest extends BaseHttpIntegrationTest {
    private final AcceptJsonContent handler;

    public AcceptJsonContentIntegrationTest(@NotNull SupportedJsonLibrary library) {
        handler = testSetup(AcceptJsonContent.class, withJsonLibrary(library), JsonCustom::customize).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static SupportedJsonLibrary[] libraries() {
        return SupportedJsonLibrary.values();
    }
    
    @Test
    public void accept_valid_json_map() {
        assertThat(post("/json/map/0", "{}")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of());

        assertThat(post("/json/map/0", "{\"foo\": 1}")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1));

        assertThat(post("/json/map/0", "{\"foo\": 1.0}")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1.0));

        assertThat(post("/json/map/0", "{\"foo\": \"bar\"}")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", "bar"));
    }

    @Test
    public void accept_valid_json_list() {
        assertThat(post("/json/list/0", "[]")).is200().hasContent("ok");
        assertEquals(handler.getIncoming(), List.of());

        assertThat(post("/json/list/0", "[1, 2, 3]")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), List.of(1, 2, 3));

        assertThat(post("/json/list/0", "[1.0, 2.5]")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), List.of(1.0, 2.5));

        assertThat(post("/json/list/0", "[\"foo\", \"bar\"]")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), List.of("foo", "bar"));

        assertThat(post("/json/list/0", "[\"foo\", \"bar\", 123]")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), List.of("foo", "bar", 123));
    }

    @Test
    public void accept_valid_json_object() {
        assertThat(post("/json/obj/0", "{\"foo\": 1}")).is200().hasContent("ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1));
    }

    @Test
    public void accept_valid_json_sample_bean() {
        assertThat(post("/json/sample_bean/0", "{\"x\": 1, \"s\": \"foo\", \"list\": [1, 2, 3]}")).is200().hasContent("ok");
        assertEquals(handler.getIncoming(), new SampleBean(1, "foo", List.of(1, 2, 3)));
    }

    @Test
    public void invalid_json() {
        assertThat(post("/json/obj/0", "{")).is400();
        assertNull(handler.getIncoming());

        assertThat(post("/json/obj/0", "")).is400();
        assertNull(handler.getIncoming());

        assertThat(post("/json/obj/0", "foo bar baz")).is400();
        assertNull(handler.getIncoming());
    }

    @Test
    public void wrong_format_json() {
        assertThat(post("/json/map/0", "[1, 2]")).is400();
        assertNull(handler.getIncoming());
    }
}
