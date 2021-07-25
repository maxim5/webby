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
// JTE: must be public
public class JteExample {
    private static final Supplier<TemplateEngine> templateEngine = Suppliers.memoize(JteExample::create);

    @GET(url = "/templates/jte/hello")
    @View(template = "jte/example.jte")
    public Object example() {
        return new Page("Fancy Title", "Fancy Description");
    }

    @GET(url = "/templates/manual/jte/hello")
    public String manual_example() {
        TemplateOutput output = new StringOutput();
        Object model = example();
        templateEngine.get().render("jte/example.jte", model, output);
        return output.toString();
    }

    // JTE: must be public
    public record Page(String title, String description) {}

    private static TemplateEngine create() {
        CodeResolver codeResolver = new ResourceCodeResolver("web");
        return TemplateEngine.create(codeResolver, Path.of("build/generated/jte/examples/java"), ContentType.Html);
    }
}