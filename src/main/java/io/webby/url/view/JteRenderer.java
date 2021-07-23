package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.output.StringOutputPool;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.ResourceCodeResolver;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

public class JteRenderer implements Renderer {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Injector injector;
    private final StringOutputPool pool = new StringOutputPool();
    private TemplateEngine templateEngine;

    @Inject
    private void init() {
        try {
            Provider<TemplateEngine> provider = injector.getProvider(TemplateEngine.class);
            templateEngine = provider.get();
            log.at(Level.FINE).log("Using JTE engine provider: %s", provider);
        } catch (ConfigurationException e) {
            JteConfig config = new JteConfig();
            templateEngine = createDefault(config);
            log.at(Level.FINEST).withCause(e).log("Failed to find provider for %s", TemplateEngine.class);
            log.at(Level.FINE).log("Using default JTE engine provider from config: %s", config);
        }
    }

    @NotNull
    public RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @NotNull
    public String renderToString(@NotNull String template, @NotNull Object model) {
        StringOutput output = pool.get();
        if (model instanceof Map<?, ?> mapModel) {
            //noinspection unchecked
            templateEngine.render(template, (Map<String, Object>) mapModel, output);
        } else {
            templateEngine.render(template, model, output);
        }
        return output.toString();
    }

    public byte[] renderToBytes(@NotNull String template, @NotNull Object model) throws IOException {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        return RenderUtil.outputToBytes(output::writeTo, output.getContentLength());
    }

    public void renderToByteStream(@NotNull String template, @NotNull Object model,
                                   @NotNull Supplier<OutputStream> supplier) throws IOException {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(template, model, output);
        output.writeTo(supplier.get());
    }

    private static TemplateEngine createDefault(@NotNull JteConfig config) {
        String root = "web/jte";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String classDir = "build/generated/jte/examples/java";
        boolean utf8Byte = true;

        CodeResolver codeResolver = new ResourceCodeResolver(root, classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Path.of(classDir), ContentType.Html);
        if (utf8Byte) {
            templateEngine.setBinaryStaticContent(true);
        }
        return templateEngine;
    }
}
