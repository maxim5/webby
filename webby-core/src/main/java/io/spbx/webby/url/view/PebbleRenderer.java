package io.spbx.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.DelegatingLoader;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import io.spbx.webby.url.HandlerConfigError;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

import static io.spbx.util.base.EasyCast.castAny;
import static io.spbx.webby.url.view.EasyRender.castMapOrFail;

public class PebbleRenderer implements Renderer<PebbleTemplate> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    private PebbleEngine engine;

    @Inject
    private void init() {
        engine = helper.getOrDefault(PebbleEngine.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("Pebble hot reload enabled with template cache: %s", engine.getTemplateCache());
        }
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
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return EasyRender.writeToString(writer -> template.evaluate(writer, castMapOrFail(model, this::incompatibleError)));
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> template.evaluate(writer, castMapOrFail(model, this::incompatibleError)));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception>
            renderToByteStream(@NotNull PebbleTemplate template, @NotNull Object model) {
        return outputStream -> template.evaluate(
            new OutputStreamWriter(outputStream),
            castMapOrFail(model, this::incompatibleError)
        );
    }

    private @NotNull PebbleEngine createDefault() {
        Charset charset = settings.charset();
        List<Path> viewPaths = settings.viewPaths("pebble.view.paths");
        String suffix = settings.getOrNull("pebble.filename.suffix");
        boolean cache = settings.isHotReload() ?
            settings.getBool("pebble.dev.cache.enabled", false) :
            settings.getBool("pebble.prod.cache.enabled", true);

        List<FileLoader> loaders = viewPaths.stream()
            .map(path -> {
                FileLoader fileLoader = new FileLoader();
                fileLoader.setPrefix(path.toString());
                return fileLoader;
            }).toList();

        Loader<?> loader = loaders.size() == 1 ? loaders.getFirst() : new DelegatingLoader(castAny(loaders));
        loader.setCharset(charset.displayName());
        loader.setSuffix(suffix);

        return new PebbleEngine.Builder().loader(loader).cacheActive(cache).build();
    }

    private RenderException incompatibleError(@NotNull Object model) {
        return new RenderException(
            "Pebble engine accepts only Map<String, Object> context, but got instead: %s".formatted(model)
        );
    }
}
