package io.webby.templates;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.View;

import java.nio.file.Path;

public class Jtez {
    private final TemplateEngine templateEngine = create();

    @GET(url = "/templates/jte/hello")
    @View(template = "example.jte")
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
        templateEngine.render(template, model, output);
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
