package io.spbx.webby.demo.templates;

import com.google.common.base.Suppliers;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import io.spbx.webby.url.view.EasyRender;
import io.spbx.webby.demo.DevPaths;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

@Serve(render = Render.VELOCITY)
public class VelocityExample {
    private static final Supplier<Template> HELLO = Suppliers.memoize(() -> getTemplate("velocity/hello.vm"));

    @GET(url = "/templates/velocity/hello")
    @View(template = "velocity/hello.vm")
    public Map<String, Object> hello() {
        List<Product> products = List.of(new Product("foo", 1.23), new Product("bar", 100.00));
        return Map.of("products", products);
    }

    @GET(url = "/templates/manual/velocity/hello")
    public @NotNull String manual_hello() {
        List<Product> products = List.of(new Product("foo", 1.23), new Product("bar", 100.00));
        VelocityContext context = new VelocityContext();
        context.put("products", products);
        return EasyRender.writeToString(writer -> HELLO.get().merge(context, writer));
    }

    // Velocity: must be public
    public record Product(String name, double price) {}

    private static @NotNull Template getTemplate(@NotNull String name) {
        Properties configuration = new Properties();
        configuration.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        configuration.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "false");
        configuration.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, DevPaths.DEMO_WEB);
        configuration.setProperty(RuntimeConstants.RUNTIME_LOG, "");
        configuration.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogChute");
        return new VelocityEngine(configuration).getTemplate(name);
    }
}
