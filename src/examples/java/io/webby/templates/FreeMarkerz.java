package io.webby.templates;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.webby.url.annotate.GET;

import java.io.IOException;
import java.util.Map;

public class FreeMarkerz {
    private static final Template HELLO = getTemplate("hello.ftl");

    @GET(url = "/templates/freemarker/hello")
    public String hello() throws Exception {
        return hello("Big Joe");
    }

    @GET(url = "/templates/freemarker/hello/{name}")
    public String hello(String name) throws Exception {
        Map<String, Object> root = Map.of(
            "user", name,
            "latestProduct", new Product("products/green-mouse.html", "Green Mouse")
        );
        return renderToString(HELLO, root);
    }

    @GET(url = "/templates/freemarker/hello-bytes/{name}")
    public byte[] hello_bytes(String name) throws Exception {
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
