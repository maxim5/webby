package io.spbx.webby.url.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class MustacheJavaRenderer implements Renderer<Mustache> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    private MustacheFactory factory;

    @Inject
    private void init() {
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("Mustache.java hot reload enabled");
        } else {
            factory = helper.getOrDefault(MustacheFactory.class, this::createDefault);
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;
    }

    @Override
    public @NotNull Mustache compileTemplate(@NotNull String name) {
        // See https://github.com/spullara/mustache.java/issues/57
        MustacheFactory factory = settings.isHotReload() ?
            helper.getOrDefault(MustacheFactory.class, this::createDefault) :
            this.factory;
        return factory.compile(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull Mustache mustache, @NotNull Object model) throws Exception {
        return EasyRender.writeToString(writer -> mustache.execute(writer, model));
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Mustache mustache, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> mustache.execute(writer, model));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Mustache mustache,
                                                                              @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                mustache.execute(writer, model);
            }
        };
    }

    private @NotNull MustacheFactory createDefault() {
        List<Path> viewPaths = settings.viewPaths();
        int recursionLimit = settings.getInt("mustache-java.recursion.limit", 100);

        File fileRoot = viewPaths.stream().map(Path::toFile).findFirst().orElseThrow();
        if (viewPaths.size() > 1) {
            log.at(Level.INFO).log("Mustache.java does not support multiple source directories. Using %s", fileRoot);
        }

        DefaultMustacheFactory factory = new DefaultMustacheFactory(fileRoot);
        factory.setRecursionLimit(recursionLimit);
        return factory;
    }
}
