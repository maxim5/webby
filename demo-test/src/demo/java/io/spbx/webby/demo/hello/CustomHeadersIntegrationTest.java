package io.spbx.webby.demo.hello;

import com.google.common.io.ByteStreams;
import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.webby.testing.AssertResponse.assertThat;
import static io.spbx.webby.testing.AssertResponse.contentOf;

public class CustomHeadersIntegrationTest extends BaseHttpIntegrationTest {
    protected final CustomHeaders handler = testSetup(CustomHeaders.class).initHandler();

    @Test
    public void get_plain_text() {
        HttpResponse response = get("/headers/plain/10");
        assertThat(response)
            .is200()
            .hasContent("Hello int <b>10</b>!")
            .hasContentLength(20)
            .hasContentType("text/plain");
    }

    @Test
    public void get_xml() {
        HttpResponse response = get("/headers/xml");
        assertThat(response)
            .is200()
            .hasContent("<foo><bar/></foo>")
            .hasContentLength(17)
            .hasContentType("application/xml");
    }

    @Test
    public void get_zip_stream() throws Exception {
        HttpResponse response = get("/headers/zip");
        assertThat(response)
            .is200()
            .hasHeader(HttpConst.CONTENT_DISPOSITION, "attachment; filename=\"webby-sample.zip\"");

        Map<String, String> unzippedContent = unzipBytes(contentOf(response).array());
        assertThat(unzippedContent).containsExactly(
            "0.txt", "File content for 0",
            "1.txt", "File content for 1",
            "2.txt", "File content for 2"
        );
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
