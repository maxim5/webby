package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.webby.app.Settings;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.logging.Level;

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
        return RenderUtil.writeToString(writer -> template.process(model, writer));
    }

    @Override
    public byte[] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToBytes(writer -> template.process(model, writer));
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template, @NotNull Object model) {
        return outputStream -> template.process(model, new OutputStreamWriter(outputStream));
    }

    @NotNull
    private Configuration createDefault() {
        String root = "web";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Duration reloadDelay = settings.isHotReload() ? Duration.ofMillis(100) : Duration.ofDays(365);
        log.at(Level.FINE).log("Using FreeMarker config: %s", null);

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setTemplateLoader(new ClassTemplateLoader(classLoader, root));
        configuration.setTemplateUpdateDelayMilliseconds(reloadDelay.toMillis());
        return configuration;
    }
}
