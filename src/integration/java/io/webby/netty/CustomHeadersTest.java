package io.webby.netty;

import com.google.common.io.ByteStreams;
import com.google.inject.Injector;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.Testing;
import io.webby.hello.CustomHeaders;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CustomHeadersTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        Injector injector = Testing.testStartup(CustomHeaders.class);
        handler = injector.getInstance(NettyChannelHandler.class);
    }

    @Test
    public void get_plain_text() throws Exception {
        FullHttpResponse response = get("/headers/plain/10");
        assert200(response, "Hello int <b>10</b>!");
        assertContentLength(response, "20");
        assertContentType(response, "text/plain");
    }

    @Test
    public void get_xml() throws Exception {
        FullHttpResponse response = get("/headers/xml");
        assert200(response, "<foo><bar/></foo>");
        assertContentLength(response, "17");
        assertContentType(response, "application/xml");
    }

    @Test
    public void get_zip_stream() throws Exception {
        FullHttpResponse response = get("/headers/zip");
        assert200(response);
        assertHeader(response, "content-disposition", "attachment; filename=\"webby-sample.zip\"");
        Map<String, String> expected = Map.of(
                "0.txt", "File content for 0",
                "1.txt", "File content for 1",
                "2.txt", "File content for 2");
        Assertions.assertEquals(expected, unzipBytes(response.content().array()));
    }

    @NotNull
    private static Map<String, String> unzipBytes(byte[] bytes) throws IOException {
        Map<String, String> contents = new HashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                byte[] array = ByteStreams.toByteArray(zipInputStream);
                contents.put(entry.getName(), new String(array));
            }
        }
        return contents;
    }

    private static void assertContentLength(FullHttpResponse response, String expected) {
        assertHeader(response, "content-length", expected);
    }

    private static void assertContentType(FullHttpResponse response, String expected) {
        assertHeader(response, "content-type", expected);
    }

    private static void assertHeader(FullHttpResponse response, String name, String expected) {
        Assertions.assertEquals(expected, response.headers().get(name));
    }
}
