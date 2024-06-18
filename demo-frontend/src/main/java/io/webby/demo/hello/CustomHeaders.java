package io.webby.demo.hello;

import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Header;
import io.spbx.webby.url.annotate.Http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CustomHeaders {
    @GET(url="/headers/plain/{id}")
    @Http(contentType = "text/plain")
    public String plain_text(int id) {
        return "Hello int <b>%d</b>!".formatted(id);
    }

    @GET(url="/headers/xml")
    @Http(contentType = "application/xml")
    public String xml() {
        return "<foo><bar/></foo>";
    }

    @GET(url="/headers/zip")
    @Http(contentType = "application/zip",
          headers = {
        @Header(name="Content-Disposition", value = "attachment; filename=\"webby-sample.zip\"")
    })
    public InputStream zip_stream() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutput = new ZipOutputStream(byteStream)) {
            for (int i = 0; i < 3; i++) {
                ZipEntry entry = new ZipEntry("%d.txt".formatted(i));
                zipOutput.putNextEntry(entry);
                zipOutput.write("File content for %d".formatted(i).getBytes(Charset.defaultCharset()));
                zipOutput.closeEntry();
            }
        }
        return new ByteArrayInputStream(byteStream.toByteArray());
    }
}
