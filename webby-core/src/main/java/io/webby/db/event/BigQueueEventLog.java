package io.webby.db.event;

import com.leansoft.bigqueue.IBigQueue;
import io.webby.db.codec.Codec;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class BigQueueEventLog<E> implements EventLog<E> {
    private final IBigQueue queue;
    private final Codec<E> codec;

    public BigQueueEventLog(@NotNull IBigQueue queue, @NotNull Codec<E> codec) {
        this.queue = queue;
        this.codec = codec;
    }

    @Override
    public void append(@NotNull E event) {
        try {
            byte[] bytes = codec.writeToBytes(event);
            queue.enqueue(bytes);
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }

    @Override
    public void forEach(@NotNull Consumer<E> consumer) {
        try {
            queue.applyForEach(bytes -> {
                E event = codec.readFromBytes(bytes);
                consumer.accept(event);
            });
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }

    public long size() {
        return queue.size();
    }

    @Override
    public void forceFlush() {
    }

    @Override
    public void clearCache() {
    }

    @Override
    public void close() throws IOException {
        queue.close();
    }
}
