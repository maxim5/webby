package io.webby.url.view;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.function.Supplier;

public interface Renderer {
    @NotNull
    RenderSupport support();

    @NotNull
    String renderToString(@NotNull String template, @NotNull Object model) throws Exception;

    byte[] renderToBytes(@NotNull String template, @NotNull Object model) throws Exception;

    void renderToByteStream(@NotNull String template, @NotNull Object model,
                            @NotNull Supplier<OutputStream> supplier) throws Exception;
}
