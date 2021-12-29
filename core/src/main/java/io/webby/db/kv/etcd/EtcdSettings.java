package io.webby.db.kv.etcd;

import com.google.inject.Inject;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

public class EtcdSettings {
    private final String host;
    private final int port;

    @Inject
    public EtcdSettings(@NotNull Settings settings) {
        host = settings.getProperty("db.etcd.host", "localhost");
        port = settings.getIntProperty("db.etcd.port", 2379);
    }

    public @NotNull String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public @NotNull String endpoint() {
        return "http://%s:%d".formatted(host, port);
    }
}
