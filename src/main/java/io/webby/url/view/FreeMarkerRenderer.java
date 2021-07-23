package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.function.Supplier;
import java.util.logging.Level;

public class FreeMarkerRenderer implements Renderer {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Injector injector;
    private Configuration configuration;

    @Inject
    private void init() {
        try {
            if (injector.findBindingsByType(TypeLiteral.get(Configuration.class)).size() > 0) {
                Provider<Configuration> provider = injector.getProvider(Configuration.class);
                configuration = provider.get();
                log.at(Level.FINE).log("Using FreeMarker configuration provider: %s", provider);
                return;
            }
        } catch (ConfigurationException e) {
            log.at(Level.FINEST).withCause(e).log("Failed to find provider for %s", Configuration.class);
        }

        configuration = createDefault();
        log.at(Level.FINE).log("Using default FreeMarker provider from config: %s", null);
    }

    @Override
    public @NotNull RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull String name, @NotNull Object model) throws Exception {
        Template template = getTemplate(name);
        return RenderUtil.writeToString(writer -> template.process(model, writer));
    }

    @Override
    public byte[] renderToBytes(@NotNull String name, @NotNull Object model) throws Exception {
        Template template = getTemplate(name);
        return RenderUtil.writeToBytes(writer -> template.process(model, writer));
    }

    @Override
    public void renderToByteStream(@NotNull String name, @NotNull Object model,
                                   @NotNull Supplier<OutputStream> supplier) throws Exception {
        Template template = getTemplate(name);
        template.process(model, new OutputStreamWriter(supplier.get()));
    }

    @NotNull
    public Template getTemplate(String name) throws IOException {
        return configuration.getTemplate(name);
    }

    private static Configuration createDefault() {
        String root = "web/freemarker";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setTemplateLoader(new ClassTemplateLoader(classLoader, root));
        return configuration;
    }
}
