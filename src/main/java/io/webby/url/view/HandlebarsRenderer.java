package io.webby.url.view;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    @NotNull
    public HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;
    }

    @Override
    @NotNull
    public Template compileTemplate(@NotNull String name) throws HandlerConfigError {
        try {
            return handlebars.compile(name);
        } catch (IOException e) {
            throw new HandlerConfigError("Failed to compile Handlebars template: %s".formatted(name), e);
        }
    }

    @Override
    @NotNull
    public RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    @NotNull
    public String renderToString(@NotNull Template template, @NotNull Object model) throws Exception {
        return template.apply(model);
    }

    @Override
    public byte[] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToBytes(writer -> template.apply(model, writer));
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template, @NotNull Object model) {
        return outputStream -> template.apply(model, new OutputStreamWriter(outputStream));
    }

    @NotNull
    private Handlebars createDefault() {
        HighConcurrencyTemplateCache cache = new HighConcurrencyTemplateCache();
        cache.setReload(settings.isHotReload());
        log.at(Level.FINE).log("Using Handlebars config: %s", null);

        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/web");
        return new Handlebars(loader).with(cache);
    }
}