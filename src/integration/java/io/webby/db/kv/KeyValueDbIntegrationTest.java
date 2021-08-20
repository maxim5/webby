package io.webby.db.kv;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Injector;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

public class KeyValueDbIntegrationTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations(StorageType storageType) throws Exception {
        Path tempDir = createTempDirectory(storageType);
        KeyValueFactory dbFactory = setup(storageType, tempDir);
        KeyValueDb<Long, String> db = dbFactory.getDb("foo", Long.class, String.class);

        assertEqualsTo(db, Map.of());
        assertNotContainsAnyOf(db, Map.of(1L, "foo"));

        Assertions.assertNull(db.put(1L, "foo"));
        assertEqualsTo(db, Map.of(1L, "foo"));
        assertNotContainsAnyOf(db, Map.of(2L, "bar"));

        Assertions.assertNull(db.put(2L, "bar"));
        assertEqualsTo(db, Map.of(1L, "foo", 2L, "bar"));
        assertNotContainsAnyOf(db, Map.of(3L, "baz"));

        Assertions.assertEquals("bar", db.put(2L, "baz"));
        assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz"));
        assertNotContainsAnyOf(db, Map.of(3L, "bar"));

        db.set(3L, "foobar");
        assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
        assertNotContainsAnyOf(db, Map.of(0L, "bar"));

        Assertions.assertEquals("foobar", db.putIfAbsent(3L, "foo"));
        assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
        assertNotContainsAnyOf(db, Map.of(0L, "bar"));

        Assertions.assertEquals("foo", db.remove(1L));
        assertEqualsTo(db, Map.of(2L, "baz", 3L, "foobar"));
        assertNotContainsAnyOf(db, Map.of(1L, "foo"));

        Assertions.assertNull(db.remove(1L));
        assertEqualsTo(db, Map.of(2L, "baz", 3L, "foobar"));
        assertNotContainsAnyOf(db, Map.of(1L, "foo"));

        db.clear();
        assertEqualsTo(db, Map.of());
    }

    private static <K, V> void assertEqualsTo(@NotNull KeyValueDb<K, V> db, @NotNull Map<K, V> map) {
        Assertions.assertEquals(map.size(), db.size());
        Assertions.assertEquals(map.isEmpty(), db.isEmpty());
        Assertions.assertEquals(!map.isEmpty(), db.isNotEmpty());
        Assertions.assertEquals(map.keySet(), db.keySet());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            Assertions.assertTrue(db.containsKey(key));
            Assertions.assertTrue(db.containsValue(value));
            Assertions.assertEquals(value, db.get(key));
        }
    }

    private static <K, V> void assertNotContainsAnyOf(@NotNull KeyValueDb<K, V> db, @NotNull Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            Assertions.assertFalse(db.containsKey(key));
            Assertions.assertFalse(db.containsValue(value));
            Assertions.assertNull(db.get(key));
            Assertions.assertEquals(value, db.getOrDefault(key, value));
            Assertions.assertEquals(value, db.getOrCompute(key, () -> value));
        }
    }

    private @NotNull KeyValueFactory setup(@NotNull StorageType storageType, @NotNull Path tempDir) {
        Injector injector = Testing.testStartup(appSettings -> {
            appSettings.setStorageType(storageType);
            appSettings.setStoragePath(tempDir);
            appSettings.setProperty("db.lmdbjava.max.map.size.bytes", 64 << 10);
            appSettings.setProperty("db.lmdbjni.max.map.size.bytes", 64 << 10);

            log.at(Level.INFO).log("[Test] Temp storage path: %s", tempDir);
        });
        return injector.getInstance(KeyValueFactory.class);
    }

    private @NotNull Path createTempDirectory(@NotNull StorageType storageType) throws IOException {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String className = stack[1].getClassName();
        String testName = stack[1].getMethodName();
        return Files.createTempDirectory("%s_%s_%s.".formatted(className, testName, storageType));
    }
}
