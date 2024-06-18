package io.spbx.webby.demo.templates;

import com.google.common.base.Suppliers;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import io.spbx.webby.url.view.EasyRender;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Serve(render = Render.FREEMARKER)
// FreeMarker: allows package-local
class FreeMarkerExample {
    private static final Supplier<Template> HELLO = Suppliers.memoize(() -> getTemplate("hello.ftl"));

    @GET(url = "/templates/freemarker/hello")
    @View(template = "freemarker/hello.ftl")
    public Map<String, Object> hello() {
        return hello("Big Joe");
    }

    @GET(url = "/templates/freemarker/hello/{name}")
    @View(template = "freemarker/hello.ftl")
    public Map<String, Object> hello(String name) {
        return Map.of(
            "user", name,
            "latestProduct", new Product("products/green-mouse.html", "Green Mouse")
        );
    }

    @GET(url = "/templates/manual/freemarker/hello")
    public String manual_hello() throws Exception {
        return manual_hello("Big Joe");
    }

    @GET(url = "/templates/manual/freemarker/hello/{name}")
    public String manual_hello(String name) throws Exception {
        Map<String, Object> root = hello(name);
        return EasyRender.writeToString(writer -> HELLO.get().process(root, writer));
    }

    @GET(url = "/templates/manual/freemarker/hello-bytes/{name}")
    public byte[] manual_hello_bytes(String name) throws Exception {
        Map<String, Object> root = hello(name);
        return EasyRender.writeToBytes(writer -> HELLO.get().process(root, writer));
    }

    // FreeMarker: must be public
    public record Product(String url, String name) {}

    private static Template getTemplate(String name) {
        try {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
            configuration.setTemplateLoader(new ClassTemplateLoader(FreeMarkerExample.class, "/web/freemarker"));
            return configuration.getTemplate(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
