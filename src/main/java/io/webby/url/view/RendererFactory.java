package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.app.Settings;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class RendererFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Injector injector;
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

    // TODO: ensure singleton!
    @NotNull
    public Renderer<?> createRenderer(@NotNull Render render) {
        return switch (render) {
            case FREEMARKER -> injector.getInstance(FreeMarkerRenderer.class);
            case HANDLEBARS -> injector.getInstance(HandlebarsRenderer.class);
            case JTE -> injector.getInstance(JteRenderer.class);
            case PEBBLE -> injector.getInstance(PebbleRenderer.class);
            case ROCKER -> injector.getInstance(RockerRenderer.class);
        };
    }
}
