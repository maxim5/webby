package io.spbx.webby.demo.templates;

import io.spbx.webby.demo.DevPaths;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import org.jetbrains.annotations.NotNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Serve(render = Render.THYMELEAF)
public class ThymeleafExample {
    @GET(url = "/templates/thymeleaf/hello")
    @View(template = "thymeleaf/example.html")
    public Map<String, Object> students() {
        return Map.of(
            "students", List.of(new Student(101, "Maxim", 'M'))
        );
    }

    @GET(url = "/templates/manual/thymeleaf/hello")
    public @NotNull String manual_students() {
        String template = "thymeleaf/example.html";
        Map<String, Object> students = students();

        TemplateEngine engine = new TemplateEngine();
        engine.setCacheManager(new StandardCacheManager());
        engine.setTemplateResolver(new FileTemplateResolver());
        IContext context = new Context(Locale.getDefault(), students);
        TemplateSpec spec = new TemplateSpec(DevPaths.DEMO_WEB.resolve(template).toString(), TemplateMode.HTML);
        return engine.process(spec, context);
    }

    private record Student(int id, String name, char gender) {}
}
