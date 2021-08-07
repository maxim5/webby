package io.webby.hello;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Singleton;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.webby.url.annotate.GET;

import java.util.concurrent.*;

@Singleton
public class ReturnAsync {
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @GET(url = "r/async/futures/simple")
    Future<String> future() {
        return executor.submit(() -> "Success!");
    }

    @GET(url = "r/async/futures/completable")
    Future<String> completable() {
        return CompletableFuture.completedFuture("Complete");
    }

    @GET(url = "r/async/futures/task")
    FutureTask<String> future_task() {
        FutureTask<String> task = new FutureTask<>(() -> "Task");
        executor.submit(task);
        return task;
    }

    @GET(url = "r/async/futures/immediate")
    ListenableFuture<String> guava_immediate() {
        return Futures.immediateFuture("Now");
    }

    @GET(url = "r/async/futures/listenable")
    ListenableFuture<String> guava_listenable() {
        return Futures.submit(() -> "Listen", executor);
    }

    @GET(url = "r/async/futures/promise")
    Promise<String> netty_promise() {
        EventExecutor executor = new DefaultEventExecutor(this.executor);
        return executor.<String>newPromise().setSuccess("Promise");
    }

    @GET(url = "r/async/futures/timeout/{millis}")
    Future<String> timeout(long millis) {
        return executor.submit(() -> {
            Thread.sleep(millis);
            return "Thanks for waiting";
        });
    }

    @GET(url = "r/async/futures/error")
    Future<String> error() {
        return executor.submit(() -> {
            throw new RuntimeException();
        });
    }

    @GET(url = "r/async/futures/timeout/{millis}/error")
    Future<String> timeout_error(long millis) {
        return executor.submit(() -> {
            Thread.sleep(millis);
            throw new RuntimeException();
        });
    }
}
