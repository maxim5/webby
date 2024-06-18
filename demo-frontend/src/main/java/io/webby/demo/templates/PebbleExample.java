package io.webby.demo.templates;

import com.google.common.base.Suppliers;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import io.spbx.webby.url.view.EasyRender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
        return EasyRender.writeToString(writer -> HELLO.get().evaluate(writer, context));
    }

    @GET(url = "/templates/manual/pebble/hello/bytes")
    public byte[] manual_hello_bytes() throws IOException {
        Map<String, Object> context = hello();
        return EasyRender.writeToBytes(writer -> HELLO.get().evaluate(writer, context));
    }

    private static @NotNull PebbleTemplate getTemplate(@NotNull String templateName) {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("web/pebble");
        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();
        return engine.getTemplate(templateName);
    }
}
