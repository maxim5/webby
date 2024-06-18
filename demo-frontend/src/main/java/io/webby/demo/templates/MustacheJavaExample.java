package io.webby.demo.templates;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Suppliers;
import io.webby.demo.DevPaths;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import io.spbx.webby.url.view.EasyRender;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Serve(render = Render.MUSTACHE_JAVA)
public class MustacheJavaExample {
    private static final Supplier<Mustache> ITEMS = Suppliers.memoize(() -> getTemplate("mustache-java/items.mustache"));

    @GET(url = "/templates/mustache-java/hello")
    @View(template = "mustache-java/items.mustache")
    public Map<String, Object> items() {
        return Map.of("items", List.of(
                new Item("Foo", 10.0, List.of(new Feature("New!"))),
                new Item("Bar", 99.99, List.of(new Feature("Cool"), new Feature("Awesome"))),
                new Item("Qux", 1.50, List.of()),
                new Item("Baz", 500, List.of(new Feature("Old")))
        ));
    }

    @GET(url = "/templates/manual/mustache-java/hello")
    public @NotNull String manual_items() {
        Map<String, Object> items = items();
        return EasyRender.writeToString(writer -> ITEMS.get().execute(writer, items));
    }

    // MustacheJava: can be private
    private record Item(String name, double price, List<Feature> features) {}
    private record Feature(String description) {}

    private static @NotNull Mustache getTemplate(@NotNull String name) {
        MustacheFactory factory = new DefaultMustacheFactory();
        return factory.compile(Paths.get(DevPaths.DEMO_WEB, name).toUri().toString());
    }
}
