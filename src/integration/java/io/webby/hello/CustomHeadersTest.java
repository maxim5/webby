package io.webby.hello;

import com.google.common.io.ByteStreams;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.BaseHttpIntegrationTest;
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

import static io.webby.AssertResponse.*;

public class CustomHeadersTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(CustomHeaders.class);
    }

    @Test
    public void get_plain_text() throws Exception {
        HttpResponse response = get("/headers/plain/10");
        assert200(response, "Hello int <b>10</b>!");
        assertContentLength(response, "20");
        assertContentType(response, "text/plain");
    }

    @Test
    public void get_xml() throws Exception {
        HttpResponse response = get("/headers/xml");
        assert200(response, "<foo><bar/></foo>");
        assertContentLength(response, "17");
        assertContentType(response, "application/xml");
    }

    @Test
    public void get_zip_stream() throws Exception {
        HttpResponse response = get("/headers/zip");
        assert200(response);
        assertHeader(response, "content-disposition", "attachment; filename=\"webby-sample.zip\"");
        Map<String, String> expected = Map.of(
                "0.txt", "File content for 0",
                "1.txt", "File content for 1",
                "2.txt", "File content for 2");
        Assertions.assertEquals(expected, unzipBytes(content(response).array()));
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

    private static void assertContentLength(HttpResponse response, String expected) {
        assertHeader(response, "content-length", expected);
    }

    private static void assertContentType(HttpResponse response, String expected) {
        assertHeader(response, "content-type", expected);
    }

    private static void assertHeader(HttpResponse response, String name, String expected) {
        Assertions.assertEquals(expected, response.headers().get(name));
    }
}
