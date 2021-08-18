package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;

import static io.webby.util.Rethrow.Functions.rethrow;

public class FreeMarkerRenderer implements Renderer<Template> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    private Configuration configuration;

    @Inject
    private void init() {
        configuration = helper.getOrDefault(Configuration.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("FreeMarker hot reload enabled with update delay = %d ms",
                    configuration.getTemplateUpdateDelayMilliseconds());
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
            return configuration.getTemplate(name);
        } catch (IOException e) {
            throw new HandlerConfigError("Failed to compile FreeMarker template: %s".formatted(name), e);
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
        return EasyRender.writeToString(writer -> template.process(model, writer));
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> template.process(model, writer));
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template, @NotNull Object model) {
        return outputStream -> template.process(model, new OutputStreamWriter(outputStream));
    }

    @NotNull
    private Configuration createDefault() {
        TemplateLoader[] loaders = settings.viewPaths()
                .stream()
                .map(Path::toFile)
                .map(rethrow(FileTemplateLoader::new))
                .toArray(TemplateLoader[]::new);
        TemplateLoader templateLoader = loaders.length == 1 ? loaders[0] : new MultiTemplateLoader(loaders);

        Version version = new Version(settings.getProperty("freemarker.version", Configuration.getVersion()));

        Duration reloadDelay = settings.isHotReload() ?
                Duration.ofMillis(settings.getIntProperty("freemarker.dev.reload.millis", 100)) :
                Duration.ofDays(settings.getIntProperty("freemarker.prod.reload.days", 365));

        Configuration configuration = new Configuration(version);
        configuration.setTemplateLoader(templateLoader);
        configuration.setTemplateUpdateDelayMilliseconds(reloadDelay.toMillis());
        return configuration;
    }
}
