package io.spbx.util.io;

import com.google.common.flogger.FluentLogger;
import io.spbx.util.base.EasyPrimitives;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static io.spbx.util.base.EasyNulls.firstNonNull;
import static io.spbx.util.base.EasyNulls.firstNonNullIfExist;
import static io.spbx.util.base.Unchecked.Suppliers.runRethrow;

@ThreadSafe
public class LiveConfig implements AutoCloseable {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final long EVENT_TOLERANCE = 100;

    private final Path config;
    private final WatchService watchService;
    private final Thread monitorThread;
    private final AtomicReference<Properties> reference = new AtomicReference<>(new Properties());
    private volatile long lastEventMillis;

    private LiveConfig(@NotNull Path configPath) {
        assert !configPath.toFile().isDirectory() : "Config is a directory: " + configPath;
        config = configPath.normalize().toAbsolutePath();
        watchService = runRethrow(() -> FileSystems.getDefault().newWatchService());
        monitorThread = new Thread(() -> {
            try {
                registerWatchService(config.getParent());
            } catch (InterruptedException e) {
                log.at(Level.WARNING).log("File monitor interrupted. %s", this);
            } catch (IOException e) {
                Unchecked.rethrow(e);
            } catch (Throwable e) {
                Unchecked.rethrow(e);
            }
        });
        monitorThread.setDaemon(true);
    }

    public static @NotNull LiveConfig idle(@NotNull Path config) {
        LiveConfig liveConfig = new LiveConfig(config);
        liveConfig.updatePropertiesMap();
        return liveConfig;
    }

    public static @NotNull LiveConfig startMonitor(@NotNull Path config) {
        LiveConfig liveConfig = new LiveConfig(config);
        liveConfig.updatePropertiesMap();
        liveConfig.startMonitorThread();
        return liveConfig;
    }

    public void startMonitorThread() {
        assert !monitorThread.isAlive() && !monitorThread.isInterrupted() : "Monitor thread already started";
        monitorThread.start();
    }

    @Override
    public void close() {
        assert monitorThread.isAlive() : "Monitor thread has not started yet";
        log.at(Level.INFO).log("Closing %s ...", this);
        try {
            monitorThread.interrupt();
            watchService.close();
        } catch (IOException e) {
            Unchecked.rethrow(e);
        }
    }

    public @Nullable String getOrNull(@NotNull String key) {
        return firstNonNullIfExist(reference.get().getProperty(key), () -> System.getProperty(key));
    }
    public @NotNull String get(@NotNull String key, @NotNull String def) { return firstNonNull(getOrNull(key), def); }
    public @NotNull String get(@NotNull Property property) { return get(property.key(), property.def()); }

    public int getIntOrNull(@NotNull String key) { return getInt(key, 0); }
    public int getInt(@NotNull String key, int def) { return EasyPrimitives.parseIntSafe(getOrNull(key), def); }
    public int getInt(@NotNull IntProperty property) { return getInt(property.key(), property.def()); }

    public long getLongOrNull(@NotNull String key) { return getLong(key, 0L); }
    public long getLong(@NotNull String key, long def) { return EasyPrimitives.parseLongSafe(getOrNull(key), def); }
    public long getLong(@NotNull LongProperty property) { return getLong(property.key(), property.def()); }

    public boolean getBoolOrFalse(@NotNull String key) { return getBool(key, false); }
    public boolean getBool(@NotNull String key, boolean def) { return EasyPrimitives.parseBoolSafe(getOrNull(key), def); }
    public boolean getBool(@NotNull BoolProperty property) { return getBool(property.key(), property.def()); }

    public record Property(@NotNull String key, @NotNull String def) {}
    public record IntProperty(@NotNull String key, int def) {}
    public record LongProperty(@NotNull String key, long def) {}
    public record BoolProperty(@NotNull String key, boolean def) {}

    private void registerWatchService(@NotNull Path directory) throws IOException, InterruptedException {
        log.at(Level.INFO).log("Starting file monitor %s ...", this);

        directory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                onEvent(event.kind(), event.context());
            }
            if (!key.reset()) {
                log.at(Level.INFO).log("Key is invalid: %s", key);
                return;
            }
        }
    }

    private void onEvent(@NotNull WatchEvent.Kind<?> kind, @NotNull Object context) {
        log.at(Level.FINER).log("WatchEvent.Kind=%s. Context=%s", kind, context);
        if (context instanceof Path path && isConfigPath(path) && checkIn()) {
            updatePropertiesMap();
        }
    }

    private boolean checkIn() {
        long millis = System.currentTimeMillis();
        if (millis - lastEventMillis >= EVENT_TOLERANCE) {
            lastEventMillis = millis;
            return true;
        }
        return false;
    }

    private boolean isConfigPath(@NotNull Path path) {
        return path.getFileName().equals(config.getFileName()) &&
               path.normalize().toAbsolutePath().equals(config);
    }

    private void updatePropertiesMap() {
        try {
            if (config.toFile().exists()) {
                Properties properties = readPropertiesMap();
                reference.set(properties);
                log.at(Level.CONFIG).log("Properties updated: size=%d", properties.size());
            } else {
                log.at(Level.CONFIG).log("Properties file not found: %s", config);
            }
        } catch (IOException e) {
            log.at(Level.WARNING).withCause(e).log("Failed to update properties: %s", e.getMessage());
        }
    }

    private @NotNull Properties readPropertiesMap() throws IOException {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(config)) {
            properties.load(reader);
        }
        return properties;
    }

    @Override
    public String toString() {
        return "LiveConfig: `%s`".formatted(config);
    }
}
