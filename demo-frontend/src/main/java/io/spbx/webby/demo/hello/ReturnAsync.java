package io.spbx.webby.demo.hello;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Singleton;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.spbx.webby.url.annotate.GET;

import java.io.OutputStream;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static io.spbx.util.base.Unchecked.Consumers.rethrow;

@Singleton
public class ReturnAsync {
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @GET(url = "/r/async/futures/simple")
    Future<String> future() {
        return executor.submit(() -> "Success!");
    }

    @GET(url = "/r/async/futures/completable")
    Future<String> completable() {
        return CompletableFuture.completedFuture("Complete");
    }

    @GET(url = "/r/async/futures/task")
    FutureTask<String> future_task() {
        FutureTask<String> task = new FutureTask<>(() -> "Task");
        executor.submit(task);
        return task;
    }

    @GET(url = "/r/async/futures/immediate")
    ListenableFuture<String> guava_immediate() {
        return Futures.immediateFuture("Now");
    }

    @GET(url = "/r/async/futures/listenable")
    ListenableFuture<String> guava_listenable() {
        return Futures.submit(() -> "Listen", executor);
    }

    @GET(url = "/r/async/futures/netty")
    io.netty.util.concurrent.Future<String> netty_future() {
        EventExecutor executor = new DefaultEventExecutor(this.executor);
        return executor.submit(() -> "Netty");
    }

    @GET(url = "/r/async/futures/promise")
    Promise<String> netty_promise() {
        EventExecutor executor = new DefaultEventExecutor(this.executor);
        return executor.<String>newPromise().setSuccess("Promise");
    }

    @GET(url = "/r/async/futures/timeout/{millis}")
    Future<String> timeout(long millis) {
        return executor.submit(() -> {
            Thread.sleep(millis);
            return "Thanks for waiting";
        });
    }

    @GET(url = "/r/async/futures/error")
    Future<String> error() {
        return executor.submit(() -> {
            throw new RuntimeException();
        });
    }

    @GET(url = "/r/async/futures/timeout/{millis}/error")
    Future<String> error_timeout(long millis) {
        return executor.submit(() -> {
            Thread.sleep(millis);
            throw new RuntimeException();
        });
    }

    @GET(url = "/r/async/consumer/simple")
    Consumer<OutputStream> consumer() {
        return rethrow(outputStream -> {
            try (outputStream) {
                outputStream.write("Output".getBytes());
                outputStream.write("SStream".getBytes(), 1, 6);
                outputStream.write('!');
            }
        });
    }

    @GET(url = "/r/async/consumer/singles/{times}")
    Consumer<OutputStream> consumer_singles(int times) {
        return rethrow(outputStream -> {
            try (outputStream) {
                for (int i = 0; i < times; ++i) {
                    outputStream.write('0' + i);
                    outputStream.flush();
                }
            }
        });
    }

    @GET(url = "/r/async/consumer/buffer/reuse/{times}/{size}/{str}")
    Consumer<OutputStream> consumer_buffer_reuse(int times, int size, String str) {
        return rethrow(outputStream -> {
            try (outputStream) {
                // Iterates over the `str` a number of `times`,
                // but uses the same buffer instance for sending over.
                // The result must be equal to `str.repeat(times)`.
                byte[] bytes = str.getBytes();
                byte[] buffer = new byte[size];
                int bufferPos = 0;
                for (int i = 0; i < times; ++i) {
                    int startPos = bufferPos;
                    for (byte b : bytes) {
                        buffer[bufferPos] = b;
                        bufferPos = (bufferPos + 1) % size;
                        if (bufferPos == 0) {
                            outputStream.write(buffer, startPos, buffer.length - startPos);
                            startPos = 0;
                        }
                    }
                    outputStream.write(buffer, startPos, bufferPos - startPos);
                }
            }
        });
    }

    @GET(url = "/r/async/consumer/timeout/{millis}")
    Consumer<OutputStream> consumer_timeout(long millis) {
        return rethrow(outputStream -> {
            try (outputStream) {
                outputStream.write("Delayed".getBytes());
                Thread.sleep(millis / 2);
                outputStream.write("Output".getBytes());
                Thread.sleep(millis / 2);
                outputStream.write('.');
            }
        });
    }
}
