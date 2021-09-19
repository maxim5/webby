package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.util.func.ThrowConsumer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.webby.url.view.EasyRender.castMapOrFail;

public class VelocityRenderer implements Renderer<Template> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    private VelocityEngine engine;

    @Inject
    private void init() {
        engine = helper.getOrDefault(VelocityEngine.class, this::createDefault);
        if (settings.isHotReload()) {
            Object property = engine.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE);
            if (property != null && Boolean.parseBoolean(property.toString())) {
                log.at(Level.CONFIG).log("Velocity hot reload may not work properly with cache enabled");
            } else {
                log.at(Level.INFO).log("Velocity hot reload enabled");
            }
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.SELF;
    }

    @Override
    public @NotNull Template compileTemplate(@NotNull String name) {
        return engine.getTemplate(name, settings.charset().name());
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull Template template, @NotNull Object model) throws Exception {
        return EasyRender.writeToString(writer -> template.merge(getVelocityContext(model), writer));
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> template.merge(getVelocityContext(model), writer));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template,
                                                                              @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                template.merge(getVelocityContext(model), writer);
            }
        };
    }

    private @NotNull VelocityEngine createDefault() {
        String viewPaths = settings.viewPaths()
                .stream()
                .map(Path::toString)
                .collect(Collectors.joining(","));

        Properties configuration = new Properties();
        configuration.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        configuration.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, String.valueOf(!settings.isHotReload()));
        configuration.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, viewPaths);
        configuration.setProperty(RuntimeConstants.RUNTIME_LOG, "");
        configuration.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogChute");
        return new VelocityEngine(configuration);
    }

    private static @NotNull VelocityContext getVelocityContext(@NotNull Object model) {
        Map<String, Object> map = castMapOrFail(model, obj ->
            new RenderException("Velocity engine accepts only Map<String, Object> context, but got instead: %s".formatted(obj))
        );
        return new VelocityContext(new HashMap<>(map));  // copy for cases when the new values are set in the template
    }
}
