package io.spbx.webby.demo.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static io.spbx.webby.testing.AssertJson.getJsonLibrary;
import static io.spbx.webby.testing.AssertJson.withJsonLibrary;
import static io.spbx.webby.testing.AssertResponse.assertThat;
import static org.junit.Assume.assumeTrue;

@Category(Parameterized.class)
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
        assertThat(response)
            .is200()
            .hasContentType(HttpConst.APPLICATION_JSON)
            .hasContent("""
                {"foo":1,"var":["foo"]}
            """.trim());
    }

    @Test
    public void json_map_value_with_slash() {
        HttpResponse response = get("/r/json/map/foo/bar");
        assertThat(response)
            .is200()
            .hasContentType(HttpConst.APPLICATION_JSON)
            .hasContent("""
                {"foo":1,"var":["foo","bar"]}
            """.trim());
    }

    @Test
    public void json_list() {
        HttpResponse response = get("/r/json/list/foo/bar");
        assertThat(response)
            .is200()
            .hasContentType(HttpConst.APPLICATION_JSON)
            .hasContent("""
                ["foo",1,["foo","bar"]]
            """.trim());
    }

    @Test
    public void json_sample_bean() {
        HttpResponse response = get("/r/json/sample_bean/bar/baz");
        assertThat(response)
            .is200()
            .hasContentType(HttpConst.APPLICATION_JSON)
            .hasJsonContent(new SampleBean(0, "bar/baz", List.of(1, 2, 3)));
    }

    @Test
    public void json_tree() {
        assumeTrue(getJsonLibrary() == SupportedJsonLibrary.GSON);
        HttpResponse response = get("/r/json/gson/element/foobar");
        assertThat(response)
            .is200()
            .hasContentType(HttpConst.APPLICATION_JSON)
            .hasContent("""
                ["f","o","o","b","a","r"]
            """.trim());
    }
}
