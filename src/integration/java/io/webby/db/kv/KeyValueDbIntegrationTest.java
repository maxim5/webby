package io.webby.db.kv;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionManager;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.User;
import io.webby.auth.user.UserAccess;
import io.webby.db.kv.chronicle.ChronicleDb;
import io.webby.db.kv.chronicle.ChronicleFactory;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.mapdb.MapDbFactory;
import io.webby.db.kv.mapdb.MapDbImpl;
import io.webby.db.kv.paldb.PalDbFactory;
import io.webby.db.kv.paldb.PalDbImpl;
import io.webby.db.model.BlobKv;
import io.webby.testing.Mocking;
import io.webby.testing.Testing;
import io.webby.testing.ext.CloseAllExtension;
import io.webby.testing.ext.EmbeddedRedisExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.testing.ext.TempDirectoryExtension;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.sql.SqlSettings.SQLITE_IN_MEMORY;
import static io.webby.testing.FakeRequests.getEx;
import static io.webby.testing.FakeRequests.postEx;
import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assertions.*;

public class KeyValueDbIntegrationTest {
    @RegisterExtension private final static CloseAllExtension CLOSE_ALL = new CloseAllExtension();
    @RegisterExtension private final static TempDirectoryExtension TEMP_DIRECTORY = new TempDirectoryExtension();
    @RegisterExtension private final static EmbeddedRedisExtension REDIS = new EmbeddedRedisExtension();
    @RegisterExtension private final static SqlDbSetupExtension SQL_DB = new SqlDbSetupExtension(SQLITE_IN_MEMORY);

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations_fixed_size_key(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

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
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations_variable_size_key(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

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
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void bulk_operations(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<String, String> db = dbFactory.getDb("foo", String.class, String.class)) {
            db.putAll(Map.of());
            db.putAll(List.of());
            db.putAll(Stream.of());
            db.putAll(array(), array());
            db.putAll(List.of(), List.of());
            assertEqualsTo(db, Map.of());

            db.putAll(Map.of("a", "1", "b", "2"));
            assertEqualsTo(db, Map.of("a", "1", "b", "2"));

            db.putAll(Map.of("b", "3", "c", "3"));
            assertEqualsTo(db, Map.of("a", "1", "b", "3", "c", "3"));

            db.putAll(List.of(Pair.of("a", "0"), Pair.of("d", "4")));
            assertEqualsTo(db, Map.of("a", "0", "b", "3", "c", "3", "d", "4"));

            db.putAll(Stream.of(Pair.of("c", "5"), Pair.of("e", "6")));
            assertEqualsTo(db, Map.of("a", "0", "b", "3", "c", "5", "d", "4", "e", "6"));

            db.putAll(array("b", "f"), array("1", "7"));
            assertEqualsTo(db, Map.of("a", "0", "b", "1", "c", "5", "d", "4", "e", "6", "f", "7"));

            db.putAll(array("c", "g"), array("2", "8"));
            assertEqualsTo(db, Map.of("a", "0", "b", "1", "c", "2", "d", "4", "e", "6", "f", "7", "g", "8"));

            assertThat(db.getAll(array())).isEmpty();
            assertThat(db.getAll(List.of())).isEmpty();

            assertThat(db.getAll(array("a", "b", "x"))).containsExactly("0", "1", null);
            assertThat(db.getAll(List.of("a", "b", "x"))).containsExactly("0", "1", null);

            assertThat(db.getAll(array("x", "y"))).containsExactly(null, null);
            assertThat(db.getAll(List.of("x", "y"))).containsExactly(null, null);

            assertThat(db.getAll(array("a", "b", "c"))).containsExactly("0", "1", "2");
            assertThat(db.getAll(List.of("a", "b", "c"))).containsExactly("0", "1", "2");

            db.removeAll(array("f", "g", "h"));
            assertEqualsTo(db, Map.of("a", "0", "b", "1", "c", "2", "d", "4", "e", "6"));

            db.removeAll(List.of("d", "e", "f"));
            assertEqualsTo(db, Map.of("a", "0", "b", "1", "c", "2"));

            db.removeAll(array());
            db.removeAll(List.of());
            assertEqualsTo(db, Map.of("a", "0", "b", "1", "c", "2"));

            db.clear();
            assertEqualsTo(db, Map.of());
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void maps_isolation(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<String, String> db1 = dbFactory.getDb("foo", String.class, String.class)) {
            try (KeyValueDb<String, String> db2 = dbFactory.getDb("bar", String.class, String.class)) {
                db1.put("a", "b");
                assertEqualsTo(db1, Map.of("a", "b"));
                assertEqualsTo(db2, Map.of());

                db2.put("c", "d");
                assertEqualsTo(db1, Map.of("a", "b"));
                assertEqualsTo(db2, Map.of("c", "d"));

                db1.remove("a");
                assertEqualsTo(db1, Map.of());
                assertEqualsTo(db2, Map.of("c", "d"));

                db2.remove("c");
                assertEqualsTo(db1, Map.of());
                assertEqualsTo(db2, Map.of());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void serialize_session(StorageType storageType) {
        Injector injector = setup(storageType);
        SessionManager sessionManager = injector.getInstance(SessionManager.class);
        KeyValueFactory dbFactory = injector.getInstance(KeyValueFactory.class);

        Session newSession = sessionManager.createNewSession(getEx("/"));
        String cookie = sessionManager.encodeSessionForCookie(newSession);
        Session existingSession = sessionManager.getSessionOrNull(cookie);
        assertEquals(newSession, existingSession);

        try (KeyValueDb<Long, Session> db = dbFactory.getDb(Session.DB_NAME, Long.class, Session.class)) {
            assertEqualsTo(db, Map.of(newSession.sessionId(), newSession));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void multi_session_sql_compatible(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<Long, Session> db = dbFactory.getDb(Session.DB_NAME, Long.class, Session.class)) {
            runMultiTest(db, 123L, Session.fromRequest(123, getEx("/foo")));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void multi_session_forced_key_value(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<Integer, Session> db = dbFactory.getDb("my-sessions", Integer.class, Session.class)) {
            runMultiTest(db,
                         Integer.MIN_VALUE,
                         Integer.MAX_VALUE,
                         Session.fromRequest(Integer.MIN_VALUE, getEx("/foo")),
                         Session.fromRequest(Integer.MAX_VALUE, postEx("/bar")));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void multi_default_user_sql_compatible(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<Long, DefaultUser> db = dbFactory.getDb(User.DB_NAME, Long.class, DefaultUser.class)) {
            runMultiTest(db, 777L, new DefaultUser(777, UserAccess.Simple));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void multi_default_user_forced_key_value(StorageType storageType) {
        KeyValueFactory dbFactory = setupFactory(storageType);

        try (KeyValueDb<Long, DefaultUser> db = dbFactory.getDb("my-users", Long.class, DefaultUser.class)) {
            runMultiTest(db, Long.MIN_VALUE, Long.MAX_VALUE,
                         new DefaultUser(777, UserAccess.Simple), new DefaultUser(0, UserAccess.Admin));
        }
    }

    @Test
    public void internal_db() {
        AgnosticKeyValueFactory dbFactory = setup(StorageType.JAVA_MAP).getInstance(AgnosticKeyValueFactory.class);

        ChronicleFactory chronicleFactory = dbFactory.getInternalFactory(StorageType.CHRONICLE_MAP);
        ChronicleDb<Integer, String> chronicleDb = chronicleFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(chronicleDb.isEmpty());

        MapDbFactory mapDbFactory = dbFactory.getInternalFactory(StorageType.MAP_DB);
        MapDbImpl<Integer, String> mapDb = mapDbFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(mapDb.isEmpty());

        PalDbFactory palDbFactory = dbFactory.getInternalFactory(StorageType.PAL_DB);
        PalDbImpl<Integer, String> palDb = palDbFactory.getInternalDb("foo", Integer.class, String.class);
        assertTrue(palDb.isEmpty());
    }

    @AfterEach
    public void tearDown() {
        Testing.Internals.terminate();
    }

    private static <K, V> void runMultiTest(@NotNull KeyValueDb<K, V> db, @NotNull K key,  @NotNull V value) {
        assertEqualsTo(db, Map.of());
        assertNotContainsAnyOf(db, Map.of(key, value));

        assertNull(db.put(key, value));
        assertEqualsTo(db, Map.of(key, value));

        assertEquals(value, db.remove(key));
        assertEqualsTo(db, Map.of());

        assertNull(db.putIfPresent(key, value));
        assertEqualsTo(db, Map.of());

        assertNull(db.putIfAbsent(key, value));
        assertEqualsTo(db, Map.of(key, value));

        assertEquals(value, db.putIfPresent(key, value));
        assertEqualsTo(db, Map.of(key, value));

        assertEquals(value, db.putIfAbsent(key, value));
        assertEqualsTo(db, Map.of(key, value));

        db.putAll(Map.of(key, value));
        assertEqualsTo(db, Map.of(key, value));

        db.removeAll(List.of(key));
        assertEqualsTo(db, Map.of());
    }

    private static <K, V> void runMultiTest(@NotNull KeyValueDb<K, V> db,
                                            @NotNull K key1, @NotNull K key2,
                                            @NotNull V value1, @NotNull V value2) {
        assertEqualsTo(db, Map.of());
        assertNotContainsAnyOf(db, Map.of(key1, value1, key2, value2));

        assertNull(db.put(key1, value1));
        assertEqualsTo(db, Map.of(key1, value1));
        assertNotContainsAnyOf(db, Map.of(key2, value2));

        assertNull(db.put(key2, value2));
        assertEqualsTo(db, Map.of(key1, value1, key2, value2));

        assertEquals(value1, db.put(key1, value2));
        assertEqualsTo(db, Map.of(key1, value2, key2, value2));

        db.set(key2, value1);
        assertEqualsTo(db, Map.of(key1, value2, key2, value1));

        assertEquals(value1, db.remove(key2));
        assertEqualsTo(db, Map.of(key1, value2));
        assertNotContainsAnyOf(db, Map.of(key2, value1));

        assertEquals(value2, db.putIfPresent(key1, value1));
        assertEqualsTo(db, Map.of(key1, value1));

        assertEquals(value1, db.putIfAbsent(key1, value1));
        assertEqualsTo(db, Map.of(key1, value1));

        db.putAll(Map.of(key1, value1, key2, value2));
        assertEqualsTo(db, Map.of(key1, value1, key2, value2));

        db.removeAll(List.of(key1, key2));
        assertEqualsTo(db, Map.of());
    }

    private static <K, V> void assertEqualsTo(@NotNull KeyValueDb<K, V> db, @NotNull Map<K, V> map) {
        assertEquals(map.size(), db.size());
        assertEquals(map.size(), db.longSize());
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

        Map<K, V> iterated = new HashMap<>(db.size());
        db.forEach(iterated::put);
        assertEquals(iterated, map);
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

    private static @NotNull KeyValueFactory setupFactory(@NotNull StorageType storageType) {
        return setup(storageType).getInstance(KeyValueFactory.class);
    }

    private static @NotNull Injector setup(@NotNull StorageType storageType) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(Session.class, DefaultUser.class, BlobKv.class);
        settings.storageSettings()
                .enableKeyValueStorage(storageType)
                .setKeyValueStoragePath(TEMP_DIRECTORY.getCurrentTempDir())
                .enableSqlStorage(SQL_DB.getSettings());
        settings.setProfileMode(false);  // not testing TrackingDbAdapter by default

        settings.setProperty("db.chronicle.default.size", 64);
        settings.setProperty("db.lmdb-java.max.map.size.bytes", 64 << 10);
        settings.setProperty("db.lmdb-jni.max.map.size.bytes", 64 << 10);

        settings.setProperty("db.swaydb.init.map.size.bytes", 64 << 10);
        settings.setProperty("db.swaydb.segment.size.bytes", 64 << 10);
        settings.setProperty("db.swaydb.appendix.flush.checkpoint.size.bytes", 1 << 10);

        settings.setProperty("db.redis.port", REDIS.getPort());

        if (storageType == StorageType.SQL_DB) {
            AutoCloseable mockedClock = Mocking.withMockedClock();
            CLOSE_ALL.addCloseable(mockedClock);
        }

        return Testing.testStartup(settings, SQL_DB.singleConnectionPoolModule());
    }
}
