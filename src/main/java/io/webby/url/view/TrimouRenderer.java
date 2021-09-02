package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.FileSystemTemplateLocator;
import org.trimou.engine.resolver.CombinedIndexResolver;
import org.trimou.handlebars.HelpersBuilder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class TrimouRenderer implements Renderer<Mustache> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    private MustacheEngine engine;

    @Inject
    private void init() {
        engine = helper.getOrDefault(MustacheEngine.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("Trimou hot reload enabled");
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.SELF;
    }

    @Override
    public @NotNull Mustache compileTemplate(@NotNull String name) {
        return engine.getMustache(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull Mustache mustache, @NotNull Object model) throws Exception {
        return mustache.render(model);
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Mustache mustache, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> mustache.render(writer, model));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Mustache mustache,
                                                                              @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                mustache.render(writer, model);
            }
        };
    }

    private @NotNull MustacheEngine createDefault() {
        List<Path> viewPaths = settings.viewPaths();
        boolean hotReload = settings.isHotReload();
        boolean skipValueEscaping = settings.getBoolProperty("trimou.skip.value.escaping");  // Disable HTML escaping
        boolean resolverEnabled = settings.getBoolProperty("trimou.combined.index.resolver.enabled");

        MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder()
                .setProperty(EngineConfigurationKey.TEMPLATE_CACHE_ENABLED, !hotReload)
                .setProperty(EngineConfigurationKey.SKIP_VALUE_ESCAPING, skipValueEscaping)
                .setProperty(CombinedIndexResolver.ENABLED_KEY, resolverEnabled)
                .registerHelpers(HelpersBuilder.extra().build());

        for (Path path : viewPaths) {
            builder.addTemplateLocator(new FileSystemTemplateLocator(1, path.toString(), "trimou"));
        }

        return builder.build();
    }
}
