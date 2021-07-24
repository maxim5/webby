package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.output.StringOutputPool;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.ResourceCodeResolver;
import io.webby.app.Settings;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

import static io.webby.url.view.RenderUtil.cast;

public class JteRenderer implements Renderer<String> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final StringOutputPool pool = new StringOutputPool();
    @Inject private InjectorHelper helper;
    @Inject private Settings settings;
    private TemplateEngine templateEngine;

    @Inject
    private void init() {
        templateEngine = helper.getOrDefault(TemplateEngine.class, () -> createDefault(new JteConfig()));
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
    @NotNull
    public String compileTemplate(@NotNull String name) {
        return name;
    }

    @Override
    @NotNull
    public RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    @NotNull
    public String renderToString(@NotNull String template, @NotNull Object model) {
        StringOutput output = pool.get();
        if (model instanceof Map<?, ?> mapModel) {
            templateEngine.render(template, cast(mapModel), output);
        } else {
            templateEngine.render(template, model, output);
        }
        return output.toString();
    }

    @Override
    public byte[] renderToBytes(@NotNull String template, @NotNull Object model) throws IOException {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        return RenderUtil.outputToBytes(output::writeTo, output.getContentLength());
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull String template, @NotNull Object model) {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        return output::writeTo;
    }

    @NotNull
    private TemplateEngine createDefault(@NotNull JteConfig config) {
        String root = "web";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String classDir = "build/generated/jte/examples/java";
        boolean utf8Byte = true;
        log.at(Level.FINE).log("Using JTE engine config: %s", config);

        CodeResolver codeResolver = new ResourceCodeResolver(root, classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Path.of(classDir), ContentType.Html);
        if (utf8Byte) {
            templateEngine.setBinaryStaticContent(true);
        }
        return templateEngine;
    }
}
