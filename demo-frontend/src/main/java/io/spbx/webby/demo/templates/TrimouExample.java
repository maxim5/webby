package io.spbx.webby.demo.templates;

import com.google.common.base.Suppliers;
import io.spbx.webby.demo.DevPaths;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import io.spbx.webby.url.view.EasyRender;
import org.jetbrains.annotations.NotNull;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.FileSystemTemplateLocator;
import org.trimou.engine.resolver.CombinedIndexResolver;
import org.trimou.handlebars.HelpersBuilder;

import java.util.List;
import java.util.function.Supplier;

// Doesn't work without --illegal-access
@Serve(render = Render.TRIMOU)
public class TrimouExample {
    private static final Supplier<Mustache> ITEMS = Suppliers.memoize(() -> getTemplate("trimou/items"));

    @GET(url = "/templates/trimou/hello")
    @View(template = "trimou/items")
    public List<Item> hello() {
        return List.of(
            new Item("Foo", 5, true),
            new Item("Bar", 15, true),
            new Item("Qux", -1, false),
            new Item("Baz", 5000, true)
        );
    }

    @GET(url = "/templates/manual/trimou/hello")
    public @NotNull String manual_hello() {
        List<Item> items = hello();
        return EasyRender.writeToString(writer -> ITEMS.get().render(writer, items));
    }

    // Trimou: can be private
    private record Item(String name, long amount, boolean isActive) {}

    private static @NotNull Mustache getTemplate(@NotNull String name) {
        MustacheEngine engine = MustacheEngineBuilder.newBuilder()
                .setProperty(EngineConfigurationKey.SKIP_VALUE_ESCAPING, true)  // Disable HTML escaping
                .setProperty(CombinedIndexResolver.ENABLED_KEY, false)  // Disable useless resolver
                .addTemplateLocator(new FileSystemTemplateLocator(1, DevPaths.DEMO_WEB, "trimou"))
                .registerHelpers(HelpersBuilder.extra().build())
                // .addGlobalData("footer", "© 2014 Trimou team")
                .build();
        return engine.getMustache(name);
    }
}
