package io.webby.templates;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.base.Suppliers;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Serve(render = Render.HANDLEBARS)
public class Handlebarz {
    public static final Supplier<Template> TEMPLATE = Suppliers.memoize(() -> compileTemplate("template"));

    @GET(url = "/templates/handlebars/hello")
    private String hello() throws IOException {
        Map<String, Object> context = Map.of(
                "name", "Maxim",
                "occupation", "Software Engineer"
        );

        return TEMPLATE.get().apply(context);
    }

    private static Template compileTemplate(String name) {
        try {
            TemplateLoader loader = new ClassPathTemplateLoader();
            loader.setPrefix("/web/handlebars");
            Handlebars handlebars = new Handlebars(loader);
            return handlebars.compile(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
