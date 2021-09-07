package io.webby.examples.hello;

import com.google.common.io.ByteStreams;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.webby.testing.AssertResponse.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomHeadersIntegrationTest extends BaseHttpIntegrationTest {
    protected final CustomHeaders handler = testSetup(CustomHeaders.class).initHandler();

    @Test
    public void get_plain_text() {
        HttpResponse response = get("/headers/plain/10");
        assert200(response, "Hello int <b>10</b>!");
        assertContentLength(response, "20");
        assertContentType(response, "text/plain");
    }

    @Test
    public void get_xml() {
        HttpResponse response = get("/headers/xml");
        assert200(response, "<foo><bar/></foo>");
        assertContentLength(response, "17");
        assertContentType(response, "application/xml");
    }

    @Test
    public void get_zip_stream() throws Exception {
        HttpResponse response = get("/headers/zip");
        assert200(response);
        assertHeaders(response, "content-disposition", "attachment; filename=\"webby-sample.zip\"");
        Map<String, String> expected = Map.of(
                "0.txt", "File content for 0",
                "1.txt", "File content for 1",
                "2.txt", "File content for 2"
        );
        assertEquals(expected, unzipBytes(content(response).array()));
    }

    private static @NotNull Map<String, String> unzipBytes(byte @NotNull [] bytes) throws IOException {
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
}
