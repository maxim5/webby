package io.webby.templates;

import com.google.common.base.Suppliers;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.View;

import java.nio.file.Path;
import java.util.function.Supplier;

@Serve(render = Render.JTE)
public class Jtez {
    private static final Supplier<TemplateEngine> templateEngine = Suppliers.memoize(Jtez::create);

    @GET(url = "/templates/jte/hello")
    @View(template = "jte/example.jte")
    public Object example() {
        return new Page("Fancy Title", "Fancy Description");
    }

    // @GET(url = "/templates/jte/hello")
    public String obsolete_example() {
        Page page = new Page("Fancy Title", "Fancy Description");
        return renderToString("example.jte", page);
    }

    private String renderToString(String template, Object model) {
        TemplateOutput output = new StringOutput();
        templateEngine.get().render(template, model, output);
        return output.toString();
    }

    public record Page(String title, String description) {}

    private static TemplateEngine create() {
        CodeResolver codeResolver = new ResourceCodeResolver("web/jte");
        TemplateEngine engine = TemplateEngine.create(codeResolver, Path.of("build/generated/jte/examples/java"), ContentType.Html);
        engine.prepareForRendering("example.jte");
        return engine;
    }
}
