package io.webby.templates;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.View;
import io.webby.url.view.RenderUtil;

import java.io.IOException;
import java.util.Map;

public class FreeMarkerz {
    private static final Template HELLO = getTemplate("hello.ftl");

    @GET(url = "/templates/freemarker/hello")
    @View(template = "hello.ftl")
    public Map<String, Object> hello() {
        return hello("Big Joe");
    }

    @GET(url = "/templates/freemarker/hello/{name}")
    @View(template = "hello.ftl")
    public Map<String, Object> hello(String name) {
        return Map.of(
                "user", name,
                "latestProduct", new Product("products/green-mouse.html", "Green Mouse")
        );
    }

    // @GET(url = "/templates/freemarker/hello")
    public String obsolete_hello() throws Exception {
        return obsolete_hello("Big Joe");
    }

    // @GET(url = "/templates/freemarker/hello/{name}")
    public String obsolete_hello(String name) throws Exception {
        Map<String, Object> root = Map.of(
            "user", name,
            "latestProduct", new Product("products/green-mouse.html", "Green Mouse")
        );
        return renderToString(HELLO, root);
    }

    @GET(url = "/templates/freemarker/hello-bytes/{name}")
    public byte[] obsolete_hello_bytes(String name) throws Exception {
        Map<String, Object> root = Map.of(
                "user", name,
                "latestProduct", new Product("products/green-mouse.html", "Green Mouse")
        );
        return renderToBytes(HELLO, root);
    }

    private static String renderToString(Template template, Map<String, Object> root) throws Exception {
        return RenderUtil.writeToString(writer -> template.process(root, writer));
    }

    private static byte[] renderToBytes(Template template, Map<String, Object> root) throws Exception {
        return RenderUtil.writeToBytes(writer -> template.process(root, writer));
    }

    // Public for FreeMarker
    public record Product(String url, String name) {}

    private static Template getTemplate(String name) {
        try {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
            configuration.setTemplateLoader(new ClassTemplateLoader(FreeMarkerz.class, "/web/freemarker"));
            return configuration.getTemplate(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
