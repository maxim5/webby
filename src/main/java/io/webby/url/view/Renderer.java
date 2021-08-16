package io.webby.url.view;

import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

public interface Renderer<T> {
    @NotNull HotReloadSupport hotReload();

    @NotNull T compileTemplate(@NotNull String name) throws HandlerConfigError;

    // Returns the most efficient method supported by this renderer.
    @NotNull RenderSupport support();

    @NotNull String renderToString(@NotNull T template, @NotNull Object model) throws Exception;

    byte @NotNull [] renderToBytes(@NotNull T template, @NotNull Object model) throws Exception;

    @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull T template, @NotNull Object model);
}
