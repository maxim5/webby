package io.spbx.webby.db.event;

import com.leansoft.bigqueue.IBigQueue;
import io.spbx.util.base.Unchecked;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.managed.FlushMode;
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
            Unchecked.rethrow(e);
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
            Unchecked.rethrow(e);
        }
    }

    public long size() {
        return queue.size();
    }

    @Override
    public void flush(@NotNull FlushMode mode) {
        queue.flush();
    }

    @Override
    public void close() {
        try {
            queue.close();
        } catch (IOException e) {
            Unchecked.rethrow(e);
        }
    }
}
