package io.webby.url.view;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.OutputStreamOutput;
import com.fizzed.rocker.runtime.StringBuilderOutput;
import io.webby.url.HandlerConfigError;
import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

import static io.webby.url.view.RenderUtil.cast;

public class RockerRenderer implements Renderer<BindableRockerModel> {
    @Override
    public @NotNull HotReloadSupport hotReload() {
        return HotReloadSupport.RECOMPILE;  // https://github.com/fizzed/rocker#hot-reloading
    }

    @Override
    @NotNull
    public BindableRockerModel compileTemplate(@NotNull String name) throws HandlerConfigError {
        return Rocker.template(name);
    }

    @Override
    @NotNull
    public RenderSupport support() {
        return RenderSupport.BYTE_ARRAY;
    }

    @Override
    @NotNull
    public String renderToString(@NotNull BindableRockerModel template, @NotNull Object model) {
        return bindIfNecessary(template, model)
                .render(StringBuilderOutput::new)
                .toString();
    }

    @Override
    public byte[] renderToBytes(@NotNull BindableRockerModel template, @NotNull Object model) {
        return bindIfNecessary(template, model)
                .render(ArrayOfByteArraysOutput::new)
                .toByteArray();
    }

    @Override
    @NotNull
    public ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull BindableRockerModel template, @NotNull Object model) {
        return stream ->
                bindIfNecessary(template, model)
                        .render((contentType, charsetName) -> new OutputStreamOutput(contentType, stream, charsetName));
    }

    @NotNull
    private static RockerModel bindIfNecessary(@NotNull BindableRockerModel template, @NotNull Object model) {
        if (model instanceof DefaultRockerModel bound) {
            return bound;
        }
        return template.bind(cast(model, RockerRenderer::incompatibleModelError));
    }

    private static RenderingException incompatibleModelError(Object model) {
        return new RenderingException(
            "Rocker engine can only bind Map<String, Object> context, but got instead: %s".formatted(model)
        );
    }
}
