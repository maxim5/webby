package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.webby.app.Settings;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static io.webby.url.view.RenderUtil.castMapOrFail;
import static io.webby.util.Casting.castAny;

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
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    @NotNull
    public String renderToString(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToString(writer -> template.evaluate(writer, castMapOrFail(model, this::incompatibleError)));
    }

    @Override
    public byte[] renderToBytes(@NotNull PebbleTemplate template, @NotNull Object model) throws Exception {
        return RenderUtil.writeToBytes(writer -> template.evaluate(writer, castMapOrFail(model, this::incompatibleError)));
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull PebbleTemplate template, @NotNull Object model) {
        return outputStream -> template.evaluate(
                new OutputStreamWriter(outputStream),
                castMapOrFail(model, this::incompatibleError)
        );
    }

    private PebbleEngine createDefault() {
        Charset charset = settings.charset();
        String viewPathsProp = settings.getProperty("pebble.view.paths");
        String suffix = settings.getProperty("pebble.filename.suffix");
        boolean cache = settings.isHotReload() ?
                settings.getBoolProperty("pebble.dev.cache.enabled", false) :
                settings.getBoolProperty("pebble.prod.cache.enabled", true);

        List<Path> viewPaths = viewPathsProp != null ?
                Arrays.stream(viewPathsProp.split(File.pathSeparator)).map(Path::of).toList() :
                settings.viewPaths();
        List<FileLoader> loaders = viewPaths.stream()
                .map(path -> {
                    FileLoader fileLoader = new FileLoader();
                    fileLoader.setPrefix(path.toString());
                    return fileLoader;
                }).toList();

        Loader<?> loader = loaders.size() == 1 ? loaders.get(0) : new DelegatingLoader(castAny(loaders));
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
