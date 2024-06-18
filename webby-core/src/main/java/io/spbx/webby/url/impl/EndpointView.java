package io.spbx.webby.url.impl;

import io.spbx.webby.url.view.Renderer;
import org.jetbrains.annotations.NotNull;

public record EndpointView<T>(@NotNull T template, @NotNull Renderer<T> renderer, @NotNull String name) {
    public static <T> @NotNull EndpointView<T> of(@NotNull Renderer<T> renderer, @NotNull String name) {
        T template = renderer.compileTemplate(name);
        return new EndpointView<>(template, renderer, name);
    }
}
