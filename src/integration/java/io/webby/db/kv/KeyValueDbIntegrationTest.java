package io.webby.db.kv;

import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.inject.Injector;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionManager;
import io.webby.db.kv.chronicle.ChronicleDb;
import io.webby.db.kv.chronicle.ChronicleFactory;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.mapdb.MapDbFactory;
import io.webby.db.kv.mapdb.MapDbImpl;
import io.webby.db.kv.paldb.PalDbFactory;
import io.webby.db.kv.paldb.PalDbImpl;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.FakeRequests.getEx;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EmbeddedRedisExtension.class)
public class KeyValueDbIntegrationTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations_fixed_size_key(StorageType storageType) throws Exception {
        Path tempDir = createTempDirectory(storageType);
        KeyValueFactory dbFactory = setupFactory(storageType, tempDir);

        try (KeyValueDb<Long, String> db = dbFactory.getDb("foo", Long.class, String.class)) {
            assertEqualsTo(db, Map.of());
            assertNotContainsAnyOf(db, Map.of(1L, "foo"));

            assertNull(db.put(1L, "foo"));
            assertEqualsTo(db, Map.of(1L, "foo"));
            assertNotContainsAnyOf(db, Map.of(2L, "bar"));

            assertNull(db.put(2L, "bar"));
            assertEqualsTo(db, Map.of(1L, "foo", 2L, "bar"));
            assertNotContainsAnyOf(db, Map.of(3L, "baz"));

            assertEquals("bar", db.put(2L, "baz"));
            assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz"));
            assertNotContainsAnyOf(db, Map.of(3L, "bar"));

            db.set(3L, "foobar");
            assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
            assertNotContainsAnyOf(db, Map.of(0L, "bar"));

            assertEquals("foobar", db.putIfAbsent(3L, "foo"));
            assertEqualsTo(db, Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
            assertNotContainsAnyOf(db, Map.of(0L, "bar"));

            assertEquals("foo", db.remove(1L));
            assertEqualsTo(db, Map.of(2L, "baz", 3L, "foobar"));
            assertNotContainsAnyOf(db, Map.of(1L, "foo"));

            assertNull(db.remove(1L));
            assertEqualsTo(db, Map.of(2L, "baz", 3L, "foobar"));
            assertNotContainsAnyOf(db, Map.of(1L, "foo"));

            db.clear();
            assertEqualsTo(db, Map.of());
        }

        cleanUp(tempDir);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations_variable_size_key(StorageType storageType) throws Exception {
        Path tempDir = createTempDirectory(storageType);
        KeyValueFactory dbFactory = setupFactory(storageType, tempDir);

        try (KeyValueDb<String, Integer> db = dbFactory.getDb("foo", String.class, Integer.class)) {
            assertEqualsTo(db, Map.of());
            assertNotContainsAnyOf(db, Map.of("foo", 1));

            assertNull(db.put("foo", 1));
            assertEqualsTo(db, Map.of("foo", 1));
            assertNotContainsAnyOf(db, Map.of("bar", 2));

            assertNull(db.put("bar", 2));
            assertEqualsTo(db, Map.of("foo", 1, "bar", 2));
            assertNotContainsAnyOf(db, Map.of("baz", 3));

            assertEquals(2, db.put("bar", 3));
            assertEqualsTo(db, Map.of("foo", 1, "bar", 3));
            assertNotContainsAnyOf(db, Map.of("baz", 2));

            db.set("foobar", 2);
            assertEqualsTo(db, Map.of("foo", 1, "bar", 3, "foobar", 2));
            assertNotContainsAnyOf(db, Map.of("baz", 0));

            assertEquals(2, db.putIfAbsent("foobar", 4));
            assertEqualsTo(db, Map.of("foo", 1, "bar", 3, "foobar", 2));
            assertNotContainsAnyOf(db, Map.of("baz", 0));

            assertEquals(1, db.remove("foo"));
            assertEqualsTo(db, Map.of("bar", 3, "foobar", 2));
            assertNotContainsAnyOf(db, Map.of("foo", 1));

            assertNull(db.remove("foo"));
            assertEqualsTo(db, Map.of("bar", 3, "foobar", 2));
            assertNotContainsAnyOf(db, Map.of("foo", 1));

            db.clear();
            assertEqualsTo(db, Map.of());
        }

        cleanUp(tempDir);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void serialize_session(StorageType storageType) throws Exception {
        Path tempDir = createTempDirectory(storageType);
        Injector injector = setup(storageType, tempDir);
        SessionManager sessionManager = injector.getInstance(SessionManager.class);
        KeyValueFactory dbFactory = injector.getInstance(KeyValueFactory.class);

        Session newSession = sessionManager.createNewSession(getEx("/"));
        String cookie = sessionManager.encodeSessionForCookie(newSession);
        Session existingSession = sessionManager.getSessionOrNull(cookie);
        assertEquals(newSession, existingSession);

        try (KeyValueDb<Long, Session> db = dbFactory.getDb("sessions", Long.class, Session.class)) {
            assertEqualsTo(db, Map.of(newSession.sessionId(), newSession));
        }

        cleanUp(tempDir);
    }

    @Test
    public void internal_db() throws Exception {
        StorageType storageType = StorageType.JAVA_MAP;
        Path tempDir = createTempDirectory(storageType);
        AgnosticKeyValueFactory dbFactory = setup(storageType, tempDir).getInstance(AgnosticKeyValueFactory.class);

        ChronicleFactory chronicleFactory = dbFactory.getInternalFactory(StorageType.CHRONICLE_MAP);
        ChronicleDb<Integer, String> chronicleDb = chronicleFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(chronicleDb.isEmpty());

        MapDbFactory mapDbFactory = dbFactory.getInternalFactory(StorageType.MAP_DB);
        MapDbImpl<Integer, String> mapDb = mapDbFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(mapDb.isEmpty());

        PalDbFactory palDbFactory = dbFactory.getInternalFactory(StorageType.PAL_DB);
        PalDbImpl<Integer, String> palDb = palDbFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(palDb.isEmpty());

        cleanUp(tempDir);
    }

    @AfterEach
    public void tearDown() {
        Testing.Internals.terminate();
        cleanUpAtExit();
    }

    private static <K, V> void assertEqualsTo(@NotNull KeyValueDb<K, V> db, @NotNull Map<K, V> map) {
        assertEquals(map.size(), db.size());
        assertEquals(map.isEmpty(), db.isEmpty());
        assertEquals(!map.isEmpty(), db.isNotEmpty());

        assertThat(Lists.newArrayList(db.keys())).containsExactlyElementsIn(map.keySet());
        assertThat(new HashSet<>(db.keySet())).containsExactlyElementsIn(map.keySet());
        assertThat(db.values()).containsExactlyElementsIn(map.values());
        assertThat(new HashSet<>(db.entrySet())).containsExactlyElementsIn(map.entrySet());
        assertEquals(db.asMap(), map);
        assertEquals(db.copyToMap(), map);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            assertTrue(db.containsKey(key));
            assertTrue(db.containsValue(value));
            assertEquals(value, db.get(key));
            assertEquals(Optional.of(value), db.getOptional(key));
        }
    }

    private static <K, V> void assertNotContainsAnyOf(@NotNull KeyValueDb<K, V> db, @NotNull Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            assertFalse(db.containsKey(key));
            assertFalse(db.containsValue(value));
            assertNull(db.get(key));
            assertTrue(db.getOptional(key).isEmpty());
            assertEquals(value, db.getOrDefault(key, value));
            assertEquals(value, db.getOrCompute(key, () -> value));
        }
    }

    private @NotNull KeyValueFactory setupFactory(@NotNull StorageType storageType, @NotNull Path tempDir) {
        return setup(storageType, tempDir).getInstance(KeyValueFactory.class);
    }

    private @NotNull Injector setup(@NotNull StorageType storageType, @NotNull Path tempDir) {
        return Testing.testStartup(settings -> {
            settings.setStorageType(storageType);
            settings.setStoragePath(tempDir);

            settings.setProperty("db.chronicle.default.size", 64);
            settings.setProperty("db.lmdb-java.max.map.size.bytes", 64 << 10);
            settings.setProperty("db.lmdb-jni.max.map.size.bytes", 64 << 10);

            settings.setProperty("db.swaydb.init.map.size.bytes", 64 << 10);
            settings.setProperty("db.swaydb.segment.size.bytes", 64 << 10);
            settings.setProperty("db.swaydb.appendix.flush.checkpoint.size.bytes", 1 << 10);

            settings.setProperty("db.redis.port", EmbeddedRedisExtension.PORT);

            log.at(Level.INFO).log("[Test] Temp storage path: %s", tempDir);
        });
    }

    private @NotNull Path createTempDirectory(@NotNull StorageType storageType) throws IOException {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String className = stack[1].getClassName();
        String testName = stack[1].getMethodName();
        return Files.createTempDirectory("%s_%s_%s.".formatted(className, testName, storageType));
    }

    private static final boolean CLEAN_UP_IF_SUCCESSFUL = true;
    private static final ArrayList<Path> TO_CLEAN = new ArrayList<>(32);

    private static void cleanUp(@NotNull Path path) {
        if (CLEAN_UP_IF_SUCCESSFUL) {
            TO_CLEAN.add(path);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void deleteAll(@NotNull Path path) {
        try {
            MoreFiles.deleteRecursively(path, RecursiveDeleteOption.ALLOW_INSECURE);
        } catch (Exception e) {
            log.at(Level.INFO).withCause(e).log("Clean-up did not complete successfully");
        }
    }

    private static void cleanUpAtExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> TO_CLEAN.forEach(KeyValueDbIntegrationTest::deleteAll)));
    }
}
