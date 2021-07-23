package io.webby.templates;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.webby.url.annotate.GET;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

public class Pebblez {
    private static final PebbleTemplate HELLO = getTemplate("hello.peb");  // Thread-safe

    @GET(url = "/templates/pebble/hello")
    public String hello() throws IOException {
        Map<String, Object> context = Map.of("name", "Maxim");
        return renderToString(HELLO, context);
    }

    @GET(url = "/templates/pebble/hello/bytes")
    public byte[] hello_bytes() throws IOException {
        Map<String, Object> context = Map.of("name", "Maxim");
        return renderToBytes(HELLO, context);
    }

    private static String renderToString(PebbleTemplate template, Map<String, Object> context) throws IOException {
        return RenderUtil.writeToString(writer -> template.evaluate(writer, context));
    }

    private static byte[] renderToBytes(PebbleTemplate template, Map<String, Object> context) throws IOException {
        return RenderUtil.writeToBytes(writer -> template.evaluate(writer, context));
    }

    @NotNull
    private static PebbleTemplate getTemplate(@NotNull String templateName) {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("web/pebble");
        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();
        return engine.getTemplate(templateName);
    }
}
