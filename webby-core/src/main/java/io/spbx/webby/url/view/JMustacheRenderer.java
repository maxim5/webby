package io.spbx.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class JMustacheRenderer implements Renderer<Template> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    private Mustache.Compiler compiler;

    @Inject
    private void init() {
        compiler = helper.getOrDefault(Mustache.Compiler.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("JMustache hot reload enabled");
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;
    }

    @Override
    public @NotNull Template compileTemplate(@NotNull String name) {
        return compiler.loadTemplate(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull Template template, @NotNull Object model) throws Exception {
        return template.execute(model);
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull Template template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> template.execute(model, writer));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull Template template,
                                                                              @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                template.execute(model, writer);
            }
        };
    }

    private @NotNull Mustache.Compiler createDefault() {
        List<Path> viewPaths = settings.viewPaths();
        boolean escapeHtml = settings.getBoolProperty("jmustache.escape.html.enabled", true);
        boolean standardsMode = settings.getBoolProperty("jmustache.standards.mode.enabled", false);
        boolean emptyStringIsFalse = settings.getBoolProperty("jmustache.empty.string.is.false.enabled", false);
        boolean zeroIsFalse = settings.getBoolProperty("jmustache.zero.is.false.enabled", false);

        Path viewPath = viewPaths.stream().findFirst().orElseThrow();
        if (viewPaths.size() > 1) {
            log.at(Level.INFO).log("JMustache does not support multiple source directories. Using %s", viewPath);
        }

        return Mustache.compiler()
            .escapeHTML(escapeHtml)
            .standardsMode(standardsMode)
            .emptyStringIsFalse(emptyStringIsFalse)
            .zeroIsFalse(zeroIsFalse)
            .withLoader(name -> new FileReader(viewPath.resolve(name).toFile()));
    }
}
