package io.spbx.webby.benchmarks.stress;

import com.google.common.flogger.FluentLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public abstract class HttpWorker implements Worker {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final long steps;
    protected final ProgressMonitor progress;
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    protected final AtomicReference<String> cookie = new AtomicReference<>();

    public HttpWorker(@NotNull Init init) {
        steps = init.steps;
        progress = init.progress;
    }

    @Override
    public void run() {
        progress.expectTotalSteps(steps);
        try {
            for (int i = 0; i < steps; i++) {
                progress.step();
                call();
            }
        } catch (Throwable throwable) {
            log.at(Level.SEVERE).withCause(throwable).log("HttpWorker crashed");
        }
    }

    protected abstract @NotNull Request request();

    protected @NotNull Request requestInSession() {
        Request.Builder builder = request().newBuilder();
        String value = cookie.get();
        if (value != null && !value.isEmpty()) {
            builder.addHeader("Cookie", value);
        }
        return builder.build();
    }

    private void call() throws Exception {
        Request request = requestInSession();
        try (Response response = client.newCall(request).execute()) {
            // System.out.println("[CLIENT] Received: " + response.code() + " "  + response.body().contentLength());
            cookie.set(String.join(";", response.headers().values("Set-Cookie")));
        }
    }

    public record Init(long steps, @NotNull ProgressMonitor progress, @NotNull Rand rand) {}

    public static HttpWorker randomListCaller(@NotNull Init init, @NotNull List<Request> requests) {
        return new HttpWorker(init) {
            @Override
            protected @NotNull Request request() {
                return requests.get(init.rand.randomInt(requests.size()));
            }
        };
    }
}
