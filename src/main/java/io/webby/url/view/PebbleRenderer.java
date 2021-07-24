package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import static io.webby.url.view.RenderUtil.cast;

public class PebbleRenderer implements Renderer<PebbleTemplate> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    private PebbleEngine engine;

    @Inject
    private void init() {
        engine = helper.getOrDefault(PebbleEngine.class, PebbleRenderer::createDefault);
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;  // https://github.com/jknack/handlebars.java#the-cache-system
    }

    @Override
    public @NotNull PebbleTemplate compileTemplate(@NotNull String name) throws HandlerConfigError {
        try {
            return engine.getTemplate(name);
        } catch (Exception e) {
            throw new HandlerConfigError("Failed to compile Pebble template: %s".formatted(name), e);
        }
    }

    @Override
    public @NotNull RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    @NotNull
    public String renderToString(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToString(writer -> template.evaluate(writer, cast(model, this::incompatibleModelError)));
    }

    @Override
    public byte[] renderToBytes(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToBytes(writer -> template.evaluate(writer, cast(model, this::incompatibleModelError)));
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull PebbleTemplate template, @NotNull Object model) {
        return outputStream -> template.evaluate(
                new OutputStreamWriter(outputStream),
                cast(model, this::incompatibleModelError)
        );
    }

    private static PebbleEngine createDefault() {
        log.at(Level.FINE).log("Using Pebble config: %s", null);
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("web/pebble");
        return new PebbleEngine.Builder().loader(loader).build();
    }

    private RenderException incompatibleModelError(@NotNull Object model) {
        return new RenderException(
            "Pebble engine accepts only Map<String, Object> context, but got instead: %s".formatted(model)
        );
    }
}
