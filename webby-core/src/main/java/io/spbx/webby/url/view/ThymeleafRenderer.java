package io.spbx.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import org.jetbrains.annotations.NotNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import static io.spbx.webby.url.view.EasyRender.castMapOrFail;

public class ThymeleafRenderer implements Renderer<String> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private InjectorHelper helper;
    @Inject private Settings settings;

    private TemplateEngine engine;

    @Inject
    private void init() {
        engine = helper.getOrDefault(TemplateEngine.class, this::createDefault);
        if (settings.isHotReload()) {
            log.at(Level.INFO).log("Thymeleaf hot reload enabled");
        }
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.SELF;
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
    public @NotNull String renderToString(@NotNull String template, @NotNull Object model) throws Exception {
        return engine.process(toTemplateSpec(template), toContext(model));
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull String template, @NotNull Object model) throws Exception {
        return EasyRender.writeToBytes(writer -> engine.process(toTemplateSpec(template), toContext(model), writer));
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull String template,
                                                                              @NotNull Object model) {
        return outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                engine.process(toTemplateSpec(template), toContext(model), writer);
            }
        };
    }

    private @NotNull IContext toContext(@NotNull Object model) {
        Locale locale = settings.getOptional("thymeleaf.context.locale.language")
            .map(Locale::forLanguageTag)
            .orElse(Locale.getDefault());
        Map<String, Object> variables = castMapOrFail(model, obj -> new RenderException(
            "Thymeleaf engine accepts only Map<String, Object> context, but got instead: %s".formatted(obj)
        ));
        return new Context(locale, variables);
    }

    private @NotNull TemplateSpec toTemplateSpec(@NotNull String template) {
        TemplateMode mode = TemplateMode.parse(settings.get("thymeleaf.template.mode", "html"));
        return new TemplateSpec(template, mode);
    }

    private @NotNull TemplateEngine createDefault() {
        List<Path> viewPaths = settings.viewPaths();
        boolean hotReload = settings.isHotReload();
        int expressionCacheMaxSize = settings.getInt("thymeleaf.cache.expression.max.size",
                                                     StandardCacheManager.DEFAULT_EXPRESSION_CACHE_MAX_SIZE);
        int templateCacheMaxSize = settings.getInt("thymeleaf.cache.template.max.size",
                                                   StandardCacheManager.DEFAULT_TEMPLATE_CACHE_MAX_SIZE);

        TemplateEngine engine = new TemplateEngine();
        viewPaths.forEach(path -> {
            FileTemplateResolver resolver = new FileTemplateResolver();
            resolver.setPrefix("%s/".formatted(path));
            engine.addTemplateResolver(resolver);
        });

        StandardCacheManager cacheManager = new StandardCacheManager();
        if (hotReload) {
            expressionCacheMaxSize = 0;
            templateCacheMaxSize = 0;
        }
        cacheManager.setExpressionCacheMaxSize(expressionCacheMaxSize);
        cacheManager.setTemplateCacheMaxSize(templateCacheMaxSize);
        engine.setCacheManager(cacheManager);

        return engine;
    }
}
