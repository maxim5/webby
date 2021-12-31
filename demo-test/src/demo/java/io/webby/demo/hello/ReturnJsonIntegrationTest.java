package io.webby.demo.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static io.webby.testing.AssertJson.*;
import static io.webby.testing.AssertResponse.*;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class ReturnJsonIntegrationTest extends BaseHttpIntegrationTest {
    public ReturnJsonIntegrationTest(@NotNull SupportedJsonLibrary library) {
        testSetup(ReturnJson.class, withJsonLibrary(library), JsonCustom::customize).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static SupportedJsonLibrary[] libraries() {
        return SupportedJsonLibrary.values();
    }

    @Test
    public void json_map() {
        HttpResponse response = get("/r/json/map/foo");
        assert200(response, """
            {"foo":1,"var":["foo"]}
        """.trim());
        assertContentType(response, APPLICATION_JSON);
    }

    @Test
    public void json_map_value_with_slash() {
        HttpResponse response = get("/r/json/map/foo/bar");
        assert200(response, """
            {"foo":1,"var":["foo","bar"]}
        """.trim());
        assertContentType(response, APPLICATION_JSON);
    }

    @Test
    public void json_list() {
        HttpResponse response = get("/r/json/list/foo/bar");
        assert200(response, """
            ["foo",1,["foo","bar"]]
        """.trim());
        assertContentType(response, APPLICATION_JSON);
    }

    @Test
    public void json_sample_bean() {
        HttpResponse response = get("/r/json/sample_bean/bar/baz");
        assert200(response);
        assertJsonValue(content(response), new SampleBean(0, "bar/baz", List.of(1, 2, 3)));
        assertContentType(response, APPLICATION_JSON);
    }

    @Test
    public void json_tree() {
        assumeTrue(getJsonLibrary() == SupportedJsonLibrary.GSON);
        HttpResponse response = get("/r/json/gson/element/foobar");
        assert200(response, """
            ["f","o","o","b","a","r"]
        """.trim());
        assertContentType(response, APPLICATION_JSON);
    }
}
