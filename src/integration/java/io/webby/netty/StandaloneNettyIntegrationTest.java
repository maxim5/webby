package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppSettings;
import io.webby.examples.Main;
import io.webby.testing.OkRequests;
import io.webby.util.func.ThrowConsumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.logging.Level;

import static io.webby.testing.OkAsserts.*;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("slow")
public class StandaloneNettyIntegrationTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final AppSettings SETTINGS = createSettingsForTest();
    @RegisterExtension private static final StandaloneNettyExtension STANDALONE = new StandaloneNettyExtension(SETTINGS);
    private static final OkRequests Ok = OkRequests.of(STANDALONE.port());

    @Test
    public void get_hello_world() {
        call(Ok.get("/"), response -> {
            assertClientCode(response, 200);
            assertClientBody(response, "Hello World!");
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
        });
    }

    @Test
    public void post_json() {
        call(Ok.postJson("/strint/foo-bar/777", "{a: 1, b: 2, c:3}"), response -> {
            assertClientCode(response, 200);
            assertClientBody(response, "Vars: str=foo-bar y=777 content=<{a=1.0, b=2.0, c=3.0}>");
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
        });
    }

    @Test
    public void async_response() {
        call(Ok.get("/r/async/futures/timeout/300"), response -> {
            assertClientCode(response, 200);
            assertClientBody(response, "Thanks for waiting");
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
        });
    }

    @Test
    public void streaming_response() {
        call(Ok.get("/headers/zip"), response -> {
            assertClientCode(response, 200);
            assertClientBody(response, 388);
            assertClientHeader(response, "Content-Type", "application/zip");
        });
    }

    private static void call(@NotNull Request request, @NotNull ThrowConsumer<Response, IOException> onSuccess) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        try (Response response = client.newCall(request).execute()) {
            log.at(Level.INFO).log("[CLIENT] Received: " + response.code() + " " + response.body().contentLength());
            onSuccess.accept(response);
        } catch (IOException e) {
            fail(e);
        }
    }

    private static @NotNull AppSettings createSettingsForTest() {
        AppSettings settings = Main.localSettings();
        settings.handlerFilter().setPackageOnly("io.webby.examples");
        return settings;
    }
}
