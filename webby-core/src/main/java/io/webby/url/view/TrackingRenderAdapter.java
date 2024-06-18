package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import io.webby.perf.stats.RenderingStatsListener;
import io.webby.perf.stats.Stat;
import io.webby.url.HandlerConfigError;
import io.spbx.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.logging.Level;

public class TrackingRenderAdapter<T> implements Renderer<T> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Renderer<T> delegate;
    private final RenderingStatsListener listener;

    public TrackingRenderAdapter(@NotNull Renderer<T> delegate, @NotNull RenderingStatsListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public @NotNull HotReloadSupport hotReload() {
        return delegate.hotReload();
    }

    @Override
    public @NotNull T compileTemplate(@NotNull String name) throws HandlerConfigError {
        return delegate.compileTemplate(name);
    }

    @Override
    public @NotNull RenderSupport support() {
        RenderSupport support = delegate.support();
        if (support == RenderSupport.BYTE_STREAM) {
            log.at(Level.INFO).log("Streaming rendering is disabled due to stats tracking: %s", delegate);
            return RenderSupport.BYTE_ARRAY;
        }
        return support;
    }

    @Override
    public @NotNull String renderToString(@NotNull T template, @NotNull Object model) throws Exception {
        long start = System.currentTimeMillis();
        String rendered = delegate.renderToString(template, model);
        long elapsedMillis = System.currentTimeMillis() - start;
        listener.report(Stat.RENDER, rendered.length(), elapsedMillis, delegate);
        return rendered;
    }

    @Override
    public byte @NotNull [] renderToBytes(@NotNull T template, @NotNull Object model) throws Exception {
        long start = System.currentTimeMillis();
        byte @NotNull [] rendered = delegate.renderToBytes(template, model);
        long elapsedMillis = System.currentTimeMillis() - start;
        listener.report(Stat.RENDER, rendered.length, elapsedMillis, delegate);
        return rendered;
    }

    @Override
    public @NotNull ThrowConsumer<OutputStream, Exception> renderToByteStream(@NotNull T template, @NotNull Object model) {
        log.at(Level.WARNING).log("Streaming rendering is called despite support value: %s", delegate);
        return delegate.renderToByteStream(template, model);
    }
}
