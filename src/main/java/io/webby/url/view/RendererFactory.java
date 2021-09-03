package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.perf.stats.impl.StatsManager;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class RendererFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    @Inject private StatsManager statsManager;

    public @NotNull Renderer<?> getRenderer(@NotNull Render render, @NotNull String viewName) {
        Renderer<?> renderer = trackIfNecessary(getRawRenderer(render));
        if (settings.isHotReload()) {
            return switch (renderer.hotReload()) {
                case SELF -> renderer;
                case RECOMPILE -> new HotReloadAdapter<>(renderer, viewName);
                case UNSUPPORTED -> {
                    log.at(Level.WARNING).log("Hot reload is unsupported for %s".formatted(render));
                    yield renderer;
                }
            };
        }
        return renderer;
    }

    private @NotNull Renderer<?> trackIfNecessary(@NotNull Renderer<?> renderer) {
        if (shouldTrack()) {
            return new TrackingRenderAdapter<>(renderer, statsManager.newRenderingStatsListener());
        }
        return renderer;
    }

    private boolean shouldTrack() {
        return settings.isProfileMode();
    }

    private @NotNull Renderer<?> getRawRenderer(@NotNull Render render) {
        return switch (render) {
            case FREEMARKER -> helper.lazySingleton(FreeMarkerRenderer.class);
            case HANDLEBARS -> helper.lazySingleton(HandlebarsRenderer.class);
            case JMUSTACHE -> helper.lazySingleton(JMustacheRenderer.class);
            case JTE -> helper.lazySingleton(JteRenderer.class);
            case MUSTACHE_JAVA -> helper.lazySingleton(MustacheJavaRenderer.class);
            case PEBBLE -> helper.lazySingleton(PebbleRenderer.class);
            case ROCKER -> helper.lazySingleton(RockerRenderer.class);
            case TRIMOU -> helper.lazySingleton(TrimouRenderer.class);
            case VELOCITY -> helper.lazySingleton(VelocityRenderer.class);
        };
    }
}
