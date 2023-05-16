package io.webby.db.kv;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionManager;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserModel;
import io.webby.db.kv.chronicle.ChronicleDb;
import io.webby.db.kv.chronicle.ChronicleFactory;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.mapdb.MapDbFactory;
import io.webby.db.kv.mapdb.MapDbImpl;
import io.webby.db.kv.paldb.PalDbFactory;
import io.webby.db.kv.paldb.PalDbImpl;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingProps;
import io.webby.testing.ext.CloseAllExtension;
import io.webby.testing.ext.EmbeddedRedisExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.testing.ext.TempDirectoryExtension;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.*;

@Tags({@Tag("sql"), @Tag("slow")})
public class KeyValueDbIntegrationTest {
    @RegisterExtension static final CloseAllExtension CLOSE_ALL = new CloseAllExtension();
    @RegisterExtension static final TempDirectoryExtension TEMP_DIRECTORY = new TempDirectoryExtension();
    @RegisterExtension static final EmbeddedRedisExtension REDIS = new EmbeddedRedisExtension();
    @RegisterExtension static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties();

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void simple_operations_fixed_size_key(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<Long, String> db = dbFactory.getDb(DbOptions.of("foo", Long.class, String.class))) {
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
    @EnumSource(DbType.class)
    public void simple_operations_variable_size_key(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<String, Integer> db = dbFactory.getDb(DbOptions.of("foo", String.class, Integer.class))) {
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
    @EnumSource(DbType.class)
    public void bulk_operations(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<String, String> db = dbFactory.getDb(DbOptions.of("foo", String.class, String.class))) {
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
    @EnumSource(DbType.class)
    public void maps_isolation(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<String, String> db1 = dbFactory.getDb(DbOptions.of("foo", String.class, String.class))) {
            try (KeyValueDb<String, String> db2 = dbFactory.getDb(DbOptions.of("bar", String.class, String.class))) {
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
    @EnumSource(DbType.class)
    public void serialize_session(DbType dbType) {
        Injector injector = setup(dbType);
        SessionManager sessionManager = injector.getInstance(SessionManager.class);
        KeyValueFactory dbFactory = injector.getInstance(KeyValueFactory.class);

        Session newSession = sessionManager.createNewSession(HttpRequestBuilder.get("/").ex());
        String cookie = sessionManager.encodeSessionForCookie(newSession);
        Session existingSession = sessionManager.getSessionOrNull(cookie);
        assertEquals(newSession, existingSession);

        try (KeyValueDb<Long, Session> db = dbFactory.getDb(DbOptions.of(Session.DB_NAME, Long.class, Session.class))) {
            assertEqualsTo(db, Map.of(newSession.sessionId(), newSession));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_session_sql_compatible(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Long, Session> options = DbOptions.of(Session.DB_NAME, Long.class, Session.class);
        try (KeyValueDb<Long, Session> db = dbFactory.getDb(options)) {
            runMultiTest(db, 123L, Session.fromRequest(123, HttpRequestBuilder.get("/foo").ex()));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_session_forced_key_value(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Integer, Session> options = DbOptions.of("my-sessions", Integer.class, Session.class);
        try (KeyValueDb<Integer, Session> db = dbFactory.getDb(options)) {
            runMultiTest(db,
                         Integer.MIN_VALUE,
                         Integer.MAX_VALUE,
                         Session.fromRequest(Integer.MIN_VALUE, HttpRequestBuilder.get("/foo").ex()),
                         Session.fromRequest(Integer.MAX_VALUE, HttpRequestBuilder.post("/bar").ex()));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_user_sql_compatible(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Integer, DefaultUser> options = DbOptions.of(UserModel.DB_NAME, Integer.class, DefaultUser.class);
        try (KeyValueDb<Integer, DefaultUser> db = dbFactory.getDb(options)) {
            runMultiTest(db, 777, TestingModels.newUser(777, UserAccess.Simple));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_user_forced_key_value(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Integer, DefaultUser> options = DbOptions.of("my-users", Integer.class, DefaultUser.class);
        try (KeyValueDb<Integer, DefaultUser> db = dbFactory.getDb(options)) {
            runMultiTest(db, Integer.MIN_VALUE, Integer.MAX_VALUE,
                         TestingModels.newUser(777, UserAccess.Simple), TestingModels.newUser(0, UserAccess.SuperAdmin));
        }
    }

    @Test
    public void internal_db() {
        AgnosticKeyValueFactory dbFactory = setup(DbType.JAVA_MAP).getInstance(AgnosticKeyValueFactory.class);

        ChronicleFactory chronicleFactory = dbFactory.getInternalFactory(DbType.CHRONICLE_MAP);
        ChronicleDb<Long, Long> chronicleDb = chronicleFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertTrue(chronicleDb.isEmpty());

        MapDbFactory mapDbFactory = dbFactory.getInternalFactory(DbType.MAP_DB);
        MapDbImpl<Long, Long> mapDb = mapDbFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertTrue(mapDb.isEmpty());

        PalDbFactory palDbFactory = dbFactory.getInternalFactory(DbType.PAL_DB);
        PalDbImpl<Long, Long> palDb = palDbFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertTrue(palDb.isEmpty());
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

    private static @NotNull KeyValueFactory setupFactory(@NotNull DbType dbType) {
        return setup(dbType).getInstance(KeyValueFactory.class);
    }

    private static @NotNull Injector setup(@NotNull DbType dbType) {
        TestingProps.assumePropIfSet("test.kv.only_type", dbType.name());

        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setPackagesOf(Testing.CORE_MODELS);
        settings.storageSettings()
            .enableKeyValue(KeyValueSettings.of(dbType, TEMP_DIRECTORY.getCurrentTempDir()));
        settings.setProfileMode(false);  // not testing TrackingDbAdapter by default

        settings.setProperty("db.chronicle.default.size", 64);
        settings.setProperty("db.lmdb-java.max.map.size.bytes", 64 << 10);
        settings.setProperty("db.lmdb-jni.max.map.size.bytes", 64 << 10);

        settings.setProperty("db.swaydb.init.map.size.bytes", 64 << 10);
        settings.setProperty("db.swaydb.segment.size.bytes", 64 << 10);
        settings.setProperty("db.swaydb.appendix.flush.checkpoint.size.bytes", 1 << 10);

        settings.setProperty("db.redis.port", REDIS.getPort());

        if (dbType == DbType.SQL_DB) {
            settings.storageSettings().enableSql(SQL.settings());
            return Testing.testStartup(settings, SQL::savepoint, SQL.combinedTestingModule());
        }

        return Testing.testStartup(settings);
    }
}
