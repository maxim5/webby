package io.spbx.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static io.spbx.util.base.EasyCast.castMap;

public class JteRenderer implements Renderer<String> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    private TemplateEngine templateEngine;

    @Inject
    private void init() {
        templateEngine = helper.getOrDefault(TemplateEngine.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("JTE hot reload enabled");
        } else {
            log.at(Level.FINE).log("Hot reload is not enabled, but JTE provides automatic hot reload support");
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.SELF;  // https://github.com/casid/jte/blob/master/DOCUMENTATION.md#hot-reloading
    }

    @Override
    public @NotNull String compileTemplate(@NotNull String name) {
        return name;
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull String template, @NotNull Object model) {
        StringOutput output = new StringOutput(AppSettings.live().getInt(Settings.SIZE_CHARS));
        if (model instanceof Map<?, ?> mapModel) {
            templateEngine.render(template, castMap(mapModel), output);
        } else {
            templateEngine.render(template, model, output);
        }
        return output.toString();
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull String template, @NotNull Object model) throws IOException {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        return EasyRender.outputToBytes(output::writeTo, output.getContentLength());
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull String template, @NotNull Object model) {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        return output::writeTo;
    }

    private @NotNull TemplateEngine createDefault() {
        List<Path> paths = settings.viewPaths("jte.view.paths");
        Path classDir = Path.of(settings.get("jte.class.directory", "build"));
        ContentType contentType = settings.getBoolOrFalse("jte.output.plain") ? ContentType.Plain : ContentType.Html;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packageName = settings.get("jte.output.package", Constants.PACKAGE_NAME_ON_DEMAND);
        boolean isUtf8 = settings.charset().equals(StandardCharsets.UTF_8);
        boolean isUtf8Byte = settings.getBool("jte.output.utf8byte.enabled", isUtf8);
        boolean precompile = settings.getBool("jte.class.precompile.enabled", settings.isProdMode());

        DirectoryCodeResolver codeResolver = paths.stream()
            .map(DirectoryCodeResolver::new)
            .findFirst().orElseThrow();
        if (paths.size() > 1) {
            log.at(Level.INFO)
                .log("JTE engine does not support multiple source directories. Using %s", codeResolver.getRoot());
        }

        if (isUtf8Byte && !isUtf8) {
            log.at(Level.INFO).log("JTE Utf8ByteOutput is enabled but the charset is %s", settings.charset());
        }

        TemplateEngine templateEngine = TemplateEngine.create(
            codeResolver,
            classDir,
            contentType,
            classLoader,
            packageName
        );
        if (isUtf8Byte) {
            templateEngine.setBinaryStaticContent(true);
        }
        if (precompile) {
            log.at(Level.INFO).log("Pre-compiling all JTE sources. This can take some time");
            templateEngine.precompileAll();
        }
        return templateEngine;
    }
}
