package io.webby.demo.templates;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.webby.demo.DevPaths;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;
import io.webby.url.annotate.View;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.function.Supplier;

@Serve(render = Render.JMUSTACHE)
public class JMustacheExample {
    private static final Supplier<Template> TEMPLATE = Unchecked.Suppliers.rethrow(() -> getTemplate("jmustache/persons.mustache"));

    @GET(url = "/templates/jmustache/hello")
    @View(template = "jmustache/persons.mustache")
    public Object persons() {
        return new Object() {
            @SuppressWarnings("unused") final Object persons = List.of(new Person("Elvis", 75), new Person("Madonna", 52));
        };
    }

    @GET(url = "/templates/manual/jmustache/hello")
    public @NotNull String manual_persons() {
        Object persons = persons();
        return TEMPLATE.get().execute(persons);
    }

    // JMustache: can be private
    private record Person(String name, int age) {}

    private static @NotNull Template getTemplate(@NotNull String name) throws FileNotFoundException {
        FileReader reader = new FileReader("%s/%s".formatted(DevPaths.DEMO_WEB, name));
        return Mustache.compiler().compile(reader);
    }
}
