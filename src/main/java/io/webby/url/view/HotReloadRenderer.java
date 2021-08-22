package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import io.webby.url.HandlerConfigError;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.logging.Level;

public record HotReloadRenderer<T>(@NotNull Renderer<T> delegate, @NotNull String viewName) implements Renderer<T> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Override
    @NotNull
    public HotReloadSupport hotReload() {
        return delegate.hotReload();
    }

    @NotNull
    public T reloadTemplate() {
        log.at(Level.FINE).log("Reloading template %s", viewName);
        return delegate.compileTemplate(viewName);
    }

    @Override
    @NotNull
    public T compileTemplate(@NotNull String name) throws HandlerConfigError {
        HandlerConfigError.failIf(!name.equals(viewName), "Expected %s view name, got %s instead".formatted(viewName, name));
        return delegate.compileTemplate(name);
    }

    @Override
    @NotNull
    public RenderSupport support() {
        return delegate.support();
    }

    @Override
    @NotNull
    public String renderToString(@NotNull T template, @NotNull Object model) throws Exception {
        return delegate.renderToString(reloadTemplate(), model);
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull T template, @NotNull Object model) throws Exception {
        return delegate.renderToBytes(reloadTemplate(), model);
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull T template, @NotNull Object model) {
        return delegate.renderToByteStream(reloadTemplate(), model);
    }
}
