package io.webby.url.view;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.url.annotate.Render;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.webby.util.base.EasyCast.castAny;

public class ManualRenderer {
    @Inject private Settings settings;
    @Inject private RendererFactory factory;

    private final Map<Key, Value> cache = new ConcurrentHashMap<>();

    public @NotNull String renderToString(@NotNull Render render, @NotNull String templateName, @NotNull Object model) {
        try {
            Value value = getCachedValue(render, templateName);
            return value.renderer.renderToString(castAny(value.template), model);
        } catch (Exception e) {
            return Rethrow.rethrow(e);
        }
    }

    public byte @NotNull [] renderToBytes(@NotNull Render render, @NotNull String templateName, @NotNull Object model) {
        try {
            Value value = getCachedValue(render, templateName);
            return value.renderer.renderToBytes(castAny(value.template), model);
        } catch (Exception e) {
            return Rethrow.rethrow(e);
        }
    }

    public @NotNull String renderToString(@NotNull String templateName, @NotNull Object model) {
        return renderToString(settings.defaultRender(), templateName, model);
    }

    public byte @NotNull [] renderToBytes(@NotNull String templateName, @NotNull Object model) {
        return renderToBytes(settings.defaultRender(), templateName, model);
    }

    private @NotNull Value getCachedValue(@NotNull Render render, @NotNull String templateName) {
        Key key = new Key(render, templateName);
        return cache.computeIfAbsent(key, k -> {
            Renderer<?> renderer = factory.getRenderer(k.render, templateName);
            Object template = renderer.compileTemplate(templateName);
            return new Value(renderer, template);
        });
    }

    private record Key(@NotNull Render render, @NotNull String templateName) {}
    private record Value(@NotNull Renderer<?> renderer, @NotNull Object template) {}
}
