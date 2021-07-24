package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class RendererFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    @NotNull
    public Renderer<?> getRenderer(@NotNull Render render, @NotNull String viewName) {
        Renderer<?> renderer = createRenderer(render);
        if (settings.isHotReload()) {
            return switch (renderer.hotReload()) {
                case SELF -> renderer;
                case RECOMPILE -> new HotReloadRenderer<>(renderer, viewName);
                case UNSUPPORTED -> {
                    log.at(Level.WARNING).log("Hot reload is unsupported for %s".formatted(render));
                    yield renderer;
                }
            };
        }
        return renderer;
    }

    @NotNull
    public Renderer<?> createRenderer(@NotNull Render render) {
        return switch (render) {
            case FREEMARKER -> helper.lazySingleton(FreeMarkerRenderer.class);
            case HANDLEBARS -> helper.lazySingleton(HandlebarsRenderer.class);
            case JTE -> helper.lazySingleton(JteRenderer.class);
            case PEBBLE -> helper.lazySingleton(PebbleRenderer.class);
            case ROCKER -> helper.lazySingleton(RockerRenderer.class);
        };
    }
}
