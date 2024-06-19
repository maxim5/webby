package io.spbx.webby.url.view;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.OutputStreamOutput;
import com.fizzed.rocker.runtime.StringBuilderOutput;
import com.google.inject.Inject;
import io.spbx.util.func.ThrowConsumer;
import io.spbx.webby.app.Settings;
import io.spbx.webby.url.HandlerConfigError;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

import static io.spbx.webby.url.view.EasyRender.castMapOrFail;

public class RockerRenderer implements Renderer<BindableRockerModel> {
    @Inject private Settings settings;

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;  // https://github.com/fizzed/rocker#hot-reloading
    }

    @Override
    public @NotNull BindableRockerModel compileTemplate(@NotNull String name) throws HandlerConfigError {
        return Rocker.template(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        return settings.isStreamingEnabled() ? RenderSupport.BYTE_STREAM : RenderSupport.BYTE_ARRAY;
    }

    @Override
    public @NotNull String renderToString(@NotNull BindableRockerModel template, @NotNull Object model) {
        return bindIfNecessary(template, model)
            .render(StringBuilderOutput::new)
            .toString();
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull BindableRockerModel template, @NotNull Object model) {
        return bindIfNecessary(template, model)
            .render(ArrayOfByteArraysOutput::new)
            .toByteArray();
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception>
    renderToByteStream(@NotNull BindableRockerModel template, @NotNull Object model) {
        return stream ->
            bindIfNecessary(template, model)
                .render((contentType, charsetName) -> new OutputStreamOutput(contentType, stream, charsetName));
    }

    private static @NotNull RockerModel bindIfNecessary(@NotNull BindableRockerModel template, @NotNull Object model) {
        if (model instanceof DefaultRockerModel bound) {
            return bound;
        }
        return template.bind(castMapOrFail(model, RockerRenderer::incompatibleModelError));
    }

    private static RenderingException incompatibleModelError(Object model) {
        return new RenderingException(
            "Rocker engine can only bind Map<String, Object> context, but got instead: %s".formatted(model)
        );
    }
}
