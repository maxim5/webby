package io.webby.examples.hello;

import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static io.webby.testing.AssertJson.withJsonLibrary;
import static io.webby.testing.AssertResponse.assert200;
import static io.webby.testing.AssertResponse.assert400;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AcceptJsonContentIntegrationTest extends BaseHttpIntegrationTest {
    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void accept_valid_json_map(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert200(post("/json/map/0", "{}"), "ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of());

        assert200(post("/json/map/0", "{\"foo\": 1}"), "ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1));

        assert200(post("/json/map/0", "{\"foo\": 1.0}"), "ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1.0));

        assert200(post("/json/map/0", "{\"foo\": \"bar\"}"), "ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", "bar"));
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void accept_valid_json_list(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert200(post("/json/list/0", "[]"));
        assertEquals(handler.getIncoming(), List.of());

        assert200(post("/json/list/0", "[1, 2, 3]"));
        assertJsonEquivalent(handler.getIncoming(), List.of(1, 2, 3));

        assert200(post("/json/list/0", "[1.0, 2.5]"));
        assertJsonEquivalent(handler.getIncoming(), List.of(1.0, 2.5));

        assert200(post("/json/list/0", "[\"foo\", \"bar\"]"));
        assertJsonEquivalent(handler.getIncoming(), List.of("foo", "bar"));

        assert200(post("/json/list/0", "[\"foo\", \"bar\", 123]"));
        assertJsonEquivalent(handler.getIncoming(), List.of("foo", "bar", 123));
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void accept_valid_json_object(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert200(post("/json/obj/0", "{\"foo\": 1}"), "ok");
        assertJsonEquivalent(handler.getIncoming(), Map.of("foo", 1));
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void accept_valid_json_sample_bean(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert200(post("/json/sample_bean/0", "{\"x\": 1, \"s\": \"foo\", \"list\": [1, 2, 3]}"), "ok");
        assertEquals(handler.getIncoming(), new SampleBean(1, "foo", List.of(1, 2, 3)));
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void invalid_json(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert400(post("/json/obj/0", "{"));
        assertNull(handler.getIncoming());

        assert400(post("/json/obj/0", ""));
        assertNull(handler.getIncoming());

        assert400(post("/json/obj/0", "foo bar baz"));
        assertNull(handler.getIncoming());
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void wrong_format_json(SupportedJsonLibrary library) {
        AcceptJsonContent handler = initHandler(library);

        assert400(post("/json/map/0", "[1, 2]"));
        assertNull(handler.getIncoming());
    }

    private @NotNull AcceptJsonContent initHandler(@NotNull SupportedJsonLibrary library) {
        return testSetup(AcceptJsonContent.class, withJsonLibrary(library), JsonCustom::customize).initHandler();
    }
}
