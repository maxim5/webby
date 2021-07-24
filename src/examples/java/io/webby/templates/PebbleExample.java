package io.webby.templates;

import com.google.common.base.Suppliers;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.View;
import io.webby.url.view.RenderUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;
import java.util.function.Supplier;

@Serve(render = Render.PEBBLE)
class PebbleExample {
    private static final Supplier<PebbleTemplate> HELLO = Suppliers.memoize(() -> getTemplate("hello.peb"));  // Thread-safe

    @GET(url = "/templates/pebble/hello")
    @View(template = "hello.peb")
    public Map<String, Object> hello() throws IOException {
        return Map.of("name", "Maxim");
    }

    @GET(url = "/templates/manual/pebble/hello")
    public String manual_hello() throws IOException {
        Map<String, Object> context = hello();
        return RenderUtil.writeToString(writer -> HELLO.get().evaluate(writer, context));
    }

    @GET(url = "/templates/manual/pebble/hello/bytes")
    public byte[] manual_hello_bytes() throws IOException {
        Map<String, Object> context = hello();
        return RenderUtil.writeToBytes(writer -> HELLO.get().evaluate(writer, context));
    }

    @NotNull
    private static PebbleTemplate getTemplate(@NotNull String templateName) {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("web/pebble");
        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();
        return engine.getTemplate(templateName);
    }
}
