package io.webby.db.codec;

import io.webby.perf.stats.CodecStatsListener;
import io.webby.perf.stats.Stat;
import io.webby.util.EasyPrimitives.IntCounter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TrackingCodecAdapter<T> implements Codec<T> {
    private final Codec<T> delegate;
    private final CodecStatsListener listener;

    public TrackingCodecAdapter(@NotNull Codec<T> delegate, @NotNull CodecStatsListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public @NotNull CodecSize size() {
        return delegate.size();
    }

    @Override
    public int sizeOf(@NotNull T instance) {
        return delegate.sizeOf(instance);
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException {
        long start = System.currentTimeMillis();
        int totalWrittenBytes = delegate.writeTo(output, instance);
        long elapsedMillis = System.currentTimeMillis() - start;
        listener.report(Stat.CODEC_WRITE, totalWrittenBytes, elapsedMillis, delegate);
        return totalWrittenBytes;
    }

    @Override
    public @NotNull T readFrom(@NotNull InputStream input, int available) throws IOException {
        IntCounter counter = new IntCounter();
        InputStream wrapper = new InputStream() {
            @Override
            public int read() throws IOException {
                int result = input.read();
                counter.value += result;
                return result;
            }

            @Override
            public int read(byte @NotNull [] b) throws IOException {
                int result = input.read(b);
                counter.value += result;
                return result;
            }

            @Override
            public int read(byte @NotNull [] b, int off, int len) throws IOException {
                int result = input.read(b, off, len);
                counter.value += result;
                return result;
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                byte[] result = input.readAllBytes();
                counter.value += result.length;
                return result;
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                byte[] result = input.readNBytes(len);
                counter.value += result.length;
                return result;
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                int result = input.readNBytes(b, off, len);
                counter.value += result;
                return result;
            }
        };

        long start = System.currentTimeMillis();
        T instance = delegate.readFrom(wrapper, available);
        long elapsedMillis = System.currentTimeMillis() - start;
        listener.report(Stat.CODEC_READ, counter.value, elapsedMillis, delegate);
        return instance;
    }
}
