package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppSettings;
import io.webby.demo.DevPaths;
import io.webby.demo.Main;
import io.webby.testing.OkRequests;
import io.webby.testing.TestingBasics;
import io.webby.util.func.ThrowConsumer;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.OkAsserts.*;
import static io.webby.testing.OkRequests.files;
import static io.webby.testing.OkRequests.json;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("slow")
public class StandaloneNettyIntegrationTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final AppSettings SETTINGS = createSettingsForTest();
    @RegisterExtension private static final StandaloneNettyExtension STANDALONE = new StandaloneNettyExtension(SETTINGS);
    private static final OkRequests Ok = OkRequests.ofLocalhost(STANDALONE.port());

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
        call(Ok.post("/strint/foo-bar/777", json("{a: 1, b: 2, c:3}")), response -> {
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

    @Test
    public void upload_file() {
        call(Ok.post("/upload/file", files(DevPaths.DEMO_WEB + "favicon.ico")), response -> {
            assertClientCode(response, 200);
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
            assertClientBody(response, """
                Mixed: content-disposition: form-data; name="favicon.ico"; filename="favicon.ico"
                content-type: application/octet-stream; charset=UTF-8
                content-length: 15406
                Completed: true
                IsInMemory: true""");
        });
    }

    @Test
    public void upload_many_files() {
        call(Ok.post("/upload/file", files(Map.of("foo.txt", new byte[64], "bar.png", new byte[64]))), response -> {
            assertClientCode(response, 200);
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
            assertClientBody(response, content -> {
                assertThat(content.split("\n\n")).asList().containsExactly(
                    """
                    Mixed: content-disposition: form-data; name="foo.txt"; filename="foo.txt"
                    content-type: application/octet-stream; charset=UTF-8
                    content-length: 64
                    Completed: true
                    IsInMemory: true""",
                    """
                    Mixed: content-disposition: form-data; name="bar.png"; filename="bar.png"
                    content-type: application/octet-stream; charset=UTF-8
                    content-length: 64
                    Completed: true
                    IsInMemory: true"""
                );
            });
        });
    }

    @Test
    public void upload_large_file() {
        call(Ok.post("/upload/file", files(Map.of("foo.png", new byte[8 << 20]))), response -> {
            assertClientCode(response, 200);
            assertClientHeader(response, "Content-Type", "text/html; charset=UTF-8");
            assertClientBody(response, """
                Mixed: content-disposition: form-data; name="foo.png"; filename="foo.png"
                content-type: application/octet-stream; charset=UTF-8
                content-length: 8388608
                Completed: true
                IsInMemory: false
                RealFile: <temp-path> DeleteAfter: <delete-after>""");
        });
    }

    @Test
    public void websocket_hello_world() {
        DefaultWebSocketListener listener = new DefaultWebSocketListener();
        websocketSession(Ok.websocket("/ws/hello"), listener, ws -> {
            ws.send("foo");
            ws.send(new ByteString("bar".getBytes()));
        });
        assertNull(listener.error());
        assertThat(listener.messages()).containsExactly("Ack foo");
    }

    private static void call(@NotNull Request request, @NotNull ThrowConsumer<Response, IOException> onSuccess) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        log.at(Level.INFO).log("[CLIENT] Sending request:\n%s", describe(request));
        try (Response response = client.newCall(request).execute()) {
            log.at(Level.INFO).log("[CLIENT] Received response:\n%s", describe(response));
            onSuccess.accept(response);
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void websocketSession(@NotNull Request request,
                                         @NotNull WebSocketListener listener,
                                         @NotNull Consumer<WebSocket> session) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        log.at(Level.INFO).log("[CLIENT] Sending request:\n%s", describe(request));
        WebSocket ws = client.newWebSocket(request, listener);
        try {
            session.accept(ws);
            TestingBasics.waitFor(200);
        } finally {
            ws.close(1000, null);
        }
    }

    private static @NotNull AppSettings createSettingsForTest() {
        AppSettings settings = Main.localSettings();
        settings.handlerFilter().setPackageOnly("io.webby.demo");
        return settings;
    }
}
