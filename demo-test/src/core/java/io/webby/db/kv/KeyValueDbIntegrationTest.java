package io.webby.db.kv;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.auth.session.DefaultSession;
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
import io.webby.testing.*;
import io.webby.testing.ext.CloseAllExtension;
import io.webby.testing.ext.EmbeddedRedisExtension;
import io.webby.testing.ext.SqlDbExtension;
import io.webby.testing.ext.TempDirectoryExtension;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBasics.array;

@Tag("slow") @Tag("integration") @Tag("sql")
public class KeyValueDbIntegrationTest {
    @RegisterExtension static final CloseAllExtension CLOSE_ALL = new CloseAllExtension();
    @RegisterExtension static final TempDirectoryExtension TEMP_DIRECTORY = new TempDirectoryExtension();
    @RegisterExtension static final EmbeddedRedisExtension REDIS = new EmbeddedRedisExtension();
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withSavepoints();

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void simple_operations_fixed_size_key(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<Long, String> db = dbFactory.getDb(DbOptions.of("foo", Long.class, String.class))) {
            assertDb(db).isEqualTo(Map.of());
            assertDb(db).notContainsAnyOf(Map.of(1L, "foo"));

            assertThat(db.put(1L, "foo")).isNull();
            assertDb(db).isEqualTo(Map.of(1L, "foo"));
            assertDb(db).notContainsAnyOf(Map.of(2L, "bar"));

            assertThat(db.put(2L, "bar")).isNull();
            assertDb(db).isEqualTo(Map.of(1L, "foo", 2L, "bar"));
            assertDb(db).notContainsAnyOf(Map.of(3L, "baz"));

            assertThat(db.put(2L, "baz")).isEqualTo("bar");
            assertDb(db).isEqualTo(Map.of(1L, "foo", 2L, "baz"));
            assertDb(db).notContainsAnyOf(Map.of(3L, "bar"));

            db.set(3L, "foobar");
            assertDb(db).isEqualTo(Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
            assertDb(db).notContainsAnyOf(Map.of(0L, "bar"));

            assertThat(db.putIfAbsent(3L, "foo")).isEqualTo("foobar");
            assertDb(db).isEqualTo(Map.of(1L, "foo", 2L, "baz", 3L, "foobar"));
            assertDb(db).notContainsAnyOf(Map.of(0L, "bar"));

            assertThat(db.remove(1L)).isEqualTo("foo");
            assertDb(db).isEqualTo(Map.of(2L, "baz", 3L, "foobar"));
            assertDb(db).notContainsAnyOf(Map.of(1L, "foo"));

            assertThat(db.remove(1L)).isNull();
            assertDb(db).isEqualTo(Map.of(2L, "baz", 3L, "foobar"));
            assertDb(db).notContainsAnyOf(Map.of(1L, "foo"));

            db.clear();
            assertDb(db).isEqualTo(Map.of());
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void simple_operations_variable_size_key(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<String, Integer> db = dbFactory.getDb(DbOptions.of("foo", String.class, Integer.class))) {
            assertDb(db).isEqualTo(Map.of());
            assertDb(db).notContainsAnyOf(Map.of("foo", 1));

            assertThat(db.put("foo", 1)).isNull();
            assertDb(db).isEqualTo(Map.of("foo", 1));
            assertDb(db).notContainsAnyOf(Map.of("bar", 2));

            assertThat(db.put("bar", 2)).isNull();
            assertDb(db).isEqualTo(Map.of("foo", 1, "bar", 2));
            assertDb(db).notContainsAnyOf(Map.of("baz", 3));

            assertThat(db.put("bar", 3)).isEqualTo(2);
            assertDb(db).isEqualTo(Map.of("foo", 1, "bar", 3));
            assertDb(db).notContainsAnyOf(Map.of("baz", 2));

            db.set("foobar", 2);
            assertDb(db).isEqualTo(Map.of("foo", 1, "bar", 3, "foobar", 2));
            assertDb(db).notContainsAnyOf(Map.of("baz", 0));

            assertThat(db.putIfAbsent("foobar", 4)).isEqualTo(2);
            assertDb(db).isEqualTo(Map.of("foo", 1, "bar", 3, "foobar", 2));
            assertDb(db).notContainsAnyOf(Map.of("baz", 0));

            assertThat(db.remove("foo")).isEqualTo(1);
            assertDb(db).isEqualTo(Map.of("bar", 3, "foobar", 2));
            assertDb(db).notContainsAnyOf(Map.of("foo", 1));

            assertThat(db.remove("foo")).isNull();
            assertDb(db).isEqualTo(Map.of("bar", 3, "foobar", 2));
            assertDb(db).notContainsAnyOf(Map.of("foo", 1));

            db.clear();
            assertDb(db).isEqualTo(Map.of());
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
            assertDb(db).isEqualTo(Map.of());

            db.putAll(Map.of("a", "1", "b", "2"));
            assertDb(db).isEqualTo(Map.of("a", "1", "b", "2"));

            db.putAll(Map.of("b", "3", "c", "3"));
            assertDb(db).isEqualTo(Map.of("a", "1", "b", "3", "c", "3"));

            db.putAll(List.of(Pair.of("a", "0"), Pair.of("d", "4")));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "3", "c", "3", "d", "4"));

            db.putAll(Stream.of(Pair.of("c", "5"), Pair.of("e", "6")));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "3", "c", "5", "d", "4", "e", "6"));

            db.putAll(array("b", "f"), array("1", "7"));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "1", "c", "5", "d", "4", "e", "6", "f", "7"));

            db.putAll(array("c", "g"), array("2", "8"));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "1", "c", "2", "d", "4", "e", "6", "f", "7", "g", "8"));

            assertThat(db.getAll(array())).isEmpty();
            assertThat(db.getAll(List.of())).isEmpty();

            assertThat(db.getAll(array("a", "b", "x"))).containsExactly("0", "1", null);
            assertThat(db.getAll(List.of("a", "b", "x"))).containsExactly("0", "1", null);

            assertThat(db.getAll(array("x", "y"))).containsExactly(null, null);
            assertThat(db.getAll(List.of("x", "y"))).containsExactly(null, null);

            assertThat(db.getAll(array("a", "b", "c"))).containsExactly("0", "1", "2");
            assertThat(db.getAll(List.of("a", "b", "c"))).containsExactly("0", "1", "2");

            db.removeAll(array("f", "g", "h"));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "1", "c", "2", "d", "4", "e", "6"));

            db.removeAll(List.of("d", "e", "f"));
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "1", "c", "2"));

            db.removeAll(array());
            db.removeAll(List.of());
            assertDb(db).isEqualTo(Map.of("a", "0", "b", "1", "c", "2"));

            db.clear();
            assertDb(db).isEqualTo(Map.of());
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void maps_isolation(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        try (KeyValueDb<String, String> db1 = dbFactory.getDb(DbOptions.of("foo", String.class, String.class))) {
            try (KeyValueDb<String, String> db2 = dbFactory.getDb(DbOptions.of("bar", String.class, String.class))) {
                db1.put("a", "b");
                assertDb(db1).isEqualTo(Map.of("a", "b"));
                assertDb(db2).isEqualTo(Map.of());

                db2.put("c", "d");
                assertDb(db1).isEqualTo(Map.of("a", "b"));
                assertDb(db2).isEqualTo(Map.of("c", "d"));

                db1.remove("a");
                assertDb(db1).isEqualTo(Map.of());
                assertDb(db2).isEqualTo(Map.of("c", "d"));

                db2.remove("c");
                assertDb(db1).isEqualTo(Map.of());
                assertDb(db2).isEqualTo(Map.of());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void serialize_default_session(DbType dbType) {
        Injector injector = setup(dbType);
        SessionManager sessionManager = injector.getInstance(SessionManager.class);
        KeyValueFactory dbFactory = injector.getInstance(KeyValueFactory.class);

        DefaultSession newSession = (DefaultSession) sessionManager.createNewSession(HttpRequestBuilder.get("/").ex());
        String cookie = sessionManager.encodeSessionForCookie(newSession);
        DefaultSession existingSession = (DefaultSession) sessionManager.getSessionOrNull(cookie);
        assertThat(existingSession).isEqualTo(newSession);

        DbOptions<Long, DefaultSession> options = DbOptions.of(DefaultSession.DB_NAME, Long.class, DefaultSession.class);
        try (KeyValueDb<Long, DefaultSession> db = dbFactory.getDb(options)) {
            assertDb(db).isEqualTo(Map.of(newSession.sessionId(), newSession));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_session_sql_compatible(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Long, DefaultSession> options = DbOptions.of(DefaultSession.DB_NAME, Long.class, DefaultSession.class);
        try (KeyValueDb<Long, DefaultSession> db = dbFactory.getDb(options)) {
            runMultiTest(db, 123L, SessionBuilder.ofId(123).build());
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_session_forced_key_value_blob(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        // Key: Long -> Integer
        DbOptions<Integer, DefaultSession> options = DbOptions.of("my-sessions", Integer.class, DefaultSession.class);
        try (KeyValueDb<Integer, DefaultSession> db = dbFactory.getDb(options)) {
            runMultiTest(db,
                         Integer.MIN_VALUE,
                         Integer.MAX_VALUE,
                         SessionBuilder.ofAnyId(Integer.MIN_VALUE).build(),
                         SessionBuilder.ofId(Integer.MAX_VALUE).build());
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_user_sql_compatible(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        DbOptions<Integer, DefaultUser> options = DbOptions.of(UserModel.DB_NAME, Integer.class, DefaultUser.class);
        try (KeyValueDb<Integer, DefaultUser> db = dbFactory.getDb(options)) {
            runMultiTest(db, 777, UserBuilder.ofId(777).withAccess(UserAccess.Simple).build());
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void multi_default_user_forced_key_value_blob(DbType dbType) {
        KeyValueFactory dbFactory = setupFactory(dbType);

        // Key: Integer -> Long
        DbOptions<Long, DefaultUser> options = DbOptions.of("my-users", Long.class, DefaultUser.class);
        try (KeyValueDb<Long, DefaultUser> db = dbFactory.getDb(options)) {
            runMultiTest(db,
                         Long.MIN_VALUE,
                         Long.MAX_VALUE,
                         UserBuilder.ofId(777).withAccess(UserAccess.Simple).build(),
                         UserBuilder.ofAnyId(0).withAccess(UserAccess.SuperAdmin).build());
        }
    }

    @Test
    public void internal_db() {
        AgnosticKeyValueFactory dbFactory = setup(DbType.JAVA_MAP).getInstance(AgnosticKeyValueFactory.class);

        ChronicleFactory chronicleFactory = dbFactory.getInternalFactory(DbType.CHRONICLE_MAP);
        ChronicleDb<Long, Long> chronicleDb = chronicleFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertThat(chronicleDb.isEmpty()).isTrue();

        MapDbFactory mapDbFactory = dbFactory.getInternalFactory(DbType.MAP_DB);
        MapDbImpl<Long, Long> mapDb = mapDbFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertThat(mapDb.isEmpty()).isTrue();

        PalDbFactory palDbFactory = dbFactory.getInternalFactory(DbType.PAL_DB);
        PalDbImpl<Long, Long> palDb = palDbFactory.getInternalDb(DbOptions.of("foo", Long.class, Long.class));
        assertThat(palDb.isEmpty()).isTrue();
    }

    private static <K, V> void runMultiTest(@NotNull KeyValueDb<K, V> db, @NotNull K key, @NotNull V value) {
        assertDb(db).isEqualTo(Map.of());
        assertDb(db).notContainsAnyOf(Map.of(key, value));

        assertThat(db.put(key, value)).isNull();
        assertDb(db).isEqualTo(Map.of(key, value));

        assertThat(db.remove(key)).isEqualTo(value);
        assertDb(db).isEqualTo(Map.of());

        assertThat(db.putIfPresent(key, value)).isNull();
        assertDb(db).isEqualTo(Map.of());

        assertThat(db.putIfAbsent(key, value)).isNull();
        assertDb(db).isEqualTo(Map.of(key, value));

        assertThat(db.putIfPresent(key, value)).isEqualTo(value);
        assertDb(db).isEqualTo(Map.of(key, value));

        assertThat(db.putIfAbsent(key, value)).isEqualTo(value);
        assertDb(db).isEqualTo(Map.of(key, value));

        db.putAll(Map.of(key, value));
        assertDb(db).isEqualTo(Map.of(key, value));

        db.removeAll(List.of(key));
        assertDb(db).isEqualTo(Map.of());
    }

    private static <K, V> void runMultiTest(@NotNull KeyValueDb<K, V> db,
                                            @NotNull K key1, @NotNull K key2,
                                            @NotNull V value1, @NotNull V value2) {
        assertDb(db).isEqualTo(Map.of());
        assertDb(db).notContainsAnyOf(Map.of(key1, value1, key2, value2));

        assertThat(db.put(key1, value1)).isNull();
        assertDb(db).isEqualTo(Map.of(key1, value1));
        assertDb(db).notContainsAnyOf(Map.of(key2, value2));

        assertThat(db.put(key2, value2)).isNull();
        assertDb(db).isEqualTo(Map.of(key1, value1, key2, value2));

        assertThat(db.put(key1, value2)).isEqualTo(value1);
        assertDb(db).isEqualTo(Map.of(key1, value2, key2, value2));

        db.set(key2, value1);
        assertDb(db).isEqualTo(Map.of(key1, value2, key2, value1));

        assertThat(db.remove(key2)).isEqualTo(value1);
        assertDb(db).isEqualTo(Map.of(key1, value2));
        assertDb(db).notContainsAnyOf(Map.of(key2, value1));

        assertThat(db.putIfPresent(key1, value1)).isEqualTo(value2);
        assertDb(db).isEqualTo(Map.of(key1, value1));

        assertThat(db.putIfAbsent(key1, value1)).isEqualTo(value1);
        assertDb(db).isEqualTo(Map.of(key1, value1));

        db.putAll(Map.of(key1, value1, key2, value2));
        assertDb(db).isEqualTo(Map.of(key1, value1, key2, value2));

        db.removeAll(List.of(key1, key2));
        assertDb(db).isEqualTo(Map.of());
    }

    @CheckReturnValue
    private static <K, V> @NotNull KeyValueDbSubject<K, V> assertDb(@NotNull KeyValueDb<K, V> db) {
        return new KeyValueDbSubject<>(db);
    }

    @CanIgnoreReturnValue
    private record KeyValueDbSubject<K, V>(@NotNull KeyValueDb<K, V> db) {
        public @NotNull KeyValueDbSubject<K, V> isEqualTo(@NotNull Map<K, V> map) {
            assertThat(db.size()).isEqualTo(map.size());
            assertThat(db.longSize()).isEqualTo(map.size());
            assertThat(db.isEmpty()).isEqualTo(map.isEmpty());
            assertThat(db.isNotEmpty()).isEqualTo(!map.isEmpty());

            assertThat(Lists.newArrayList(db.keys())).containsExactlyElementsIn(map.keySet());
            assertThat(new HashSet<>(db.keySet())).containsExactlyElementsIn(map.keySet());
            assertThat(db.values()).containsExactlyElementsIn(map.values());
            assertThat(new HashSet<>(db.entrySet())).containsExactlyElementsIn(map.entrySet());
            assertThat(map).isEqualTo(db.asMap());
            assertThat(map).isEqualTo(db.copyToMap());

            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                assertThat(db.containsKey(key)).isTrue();
                assertThat(db.containsValue(value)).isTrue();
                assertThat(db.get(key)).isEqualTo(value);
                assertThat(db.getOptional(key)).hasValue(value);
            }

            Map<K, V> iterated = new HashMap<>(db.size());
            db.forEach(iterated::put);
            assertThat(iterated).isEqualTo(map);

            return this;
        }

        public @NotNull KeyValueDbSubject<K, V> notContainsAnyOf(@NotNull Map<K, V> map) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                assertThat(db.containsKey(key)).isFalse();
                assertThat(db.containsValue(value)).isFalse();
                assertThat(db.get(key)).isNull();
                assertThat(db.getOptional(key).isEmpty()).isTrue();
                assertThat(db.getOrDefault(key, value)).isEqualTo(value);
                assertThat(db.getOrCompute(key, () -> value)).isEqualTo(value);
            }
            return this;
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
            return Testing.testStartup(settings, SQL::setUp, SQL.combinedTestingModule());
        }

        return Testing.testStartup(settings);
    }
}
