package io.spbx.webby.db.kv.etcd;

import com.google.inject.Inject;
import io.spbx.util.props.PropertyMap;
import org.jetbrains.annotations.NotNull;

public class EtcdSettings {
    private final String host;
    private final int port;

    @Inject
    public EtcdSettings(@NotNull PropertyMap properties) {
        host = properties.get("db.etcd.host", "localhost");
        port = properties.getInt("db.etcd.port", 2379);
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
