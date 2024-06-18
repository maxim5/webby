package io.spbx.webby.demo.templates;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.base.Suppliers;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Serve(render = Render.HANDLEBARS)
// Handlebars: allows package-local
class HandlebarsExample {
    public static final Supplier<Template> TEMPLATE = Suppliers.memoize(() -> compileTemplate("handlebars/template"));

    @GET(url = "/templates/handlebars/hello")
    @View(template = "handlebars/template")
    private Map<String, Object> hello() {
        // Handlebars: allows private
        // Handlebars: default resolver JavaBeanValueResolver
        record Occupation(String getPosition, String getCompany) {}

        return Map.of(
            "name", "Maxim",
            "occupation", new Occupation("Software Engineer", "Google")
        );
    }

    @GET(url = "/templates/handlebars/hello/context")
    @View(template = "handlebars/template")
    Context hello_context() {
        // Handlebars: allows private
        record Occupation(String position, String company) {}

        Map<String, Object> model = Map.of(
                "name", "Maxim",
                "occupation", new Occupation("Software Engineer", "Google")
        );
        return Context.newBuilder(model).resolver(MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE).build();
    }

    @GET(url = "/templates/manual/handlebars/hello")
    private String manual_hello() throws IOException {
        Map<String, Object> context = hello();
        return TEMPLATE.get().apply(context);
    }

    private static Template compileTemplate(String name) {
        try {
            TemplateLoader loader = new ClassPathTemplateLoader();
            loader.setPrefix("/web");
            Handlebars handlebars = new Handlebars(loader);
            return handlebars.compile(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
