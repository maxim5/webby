package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import io.webby.url.HandlerConfigError;
import io.spbx.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.logging.Level;

import static io.webby.url.HandlerConfigError.assure;

public record HotReloadAdapter<T>(@NotNull Renderer<T> delegate, @NotNull String viewName) implements Renderer<T> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return delegate.hotReload();
    }

    public @NotNull T reloadTemplate() {
        log.at(Level.FINE).log("Reloading template %s", viewName);
        return delegate.compileTemplate(viewName);
    }

    @Override
    public @NotNull T compileTemplate(@NotNull String name) throws HandlerConfigError {
        assure(name.equals(viewName), "Expected %s view name, got %s instead", viewName, name);
        return delegate.compileTemplate(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        return delegate.support();
    }

    @Override
    public @NotNull String renderToString(@NotNull T template, @NotNull Object model) throws Exception {
        return delegate.renderToString(reloadTemplate(), model);
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull T template, @NotNull Object model) throws Exception {
        return delegate.renderToBytes(reloadTemplate(), model);
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull T template, @NotNull Object model) {
        return delegate.renderToByteStream(reloadTemplate(), model);
    }
}
