package io.webby.url.view;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.cache.NullTemplateCache;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.CompositeTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.url.HandlerConfigError;
import io.spbx.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.logging.Level;

public class HandlebarsRenderer implements Renderer<Template> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    private Handlebars handlebars;

    @Inject
    private void init() {
        handlebars = helper.getOrDefault(Handlebars.class, this::createDefault);
        if (settings.isHotReload()) {
            handlebars.getCache().setReload(true);
            log.at(Level.INFO).log("Handlebars hot reload enabled with cache: %s", handlebars.getCache());
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;
    }

    @Override
    public @NotNull Template compileTemplate(@NotNull String name) throws HandlerConfigError {
        try {
            return handlebars.compile(name);
        } catch (IOException e) {
            throw new HandlerConfigError("Failed to compile Handlebars template: %s".formatted(name), e);
        }
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull Template template, @NotNull Object model) throws Exception {
        return template.apply(model);
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> template.apply(model, writer));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template, @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                template.apply(model, writer);
            }
        };
    }

    private @NotNull Handlebars createDefault() {
        String suffix = settings.getProperty("handlebars.filename.suffix", TemplateLoader.DEFAULT_SUFFIX);
        TemplateLoader[] loaders = settings.viewPaths()
            .stream()
            .map(Path::toFile)
            .map(file -> new FileTemplateLoader(file, suffix))
            .toArray(TemplateLoader[]::new);
        TemplateLoader templateLoader = loaders.length == 1 ? loaders[0] : new CompositeTemplateLoader(loaders);

        String type = settings.getProperty("handlebars.cache.type", "HighConcurrency");
        TemplateCache cache = switch (type) {
            case "HighConcurrency" -> new HighConcurrencyTemplateCache();
            case "ConcurrentMap" -> new ConcurrentMapTemplateCache();
            case "Null" -> NullTemplateCache.INSTANCE;
            default -> NullTemplateCache.INSTANCE;
        };
        cache.setReload(settings.isHotReload());

        return new Handlebars(templateLoader).with(cache);
    }
}
