package io.spbx.webby.db.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Injector;
import io.spbx.orm.api.Engine;
import io.spbx.util.testing.ext.TempDirectoryExtension;
import io.spbx.webby.app.AppLifetime;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.app.ClassFilter;
import io.spbx.webby.app.Settings.Toggle;
import io.spbx.webby.common.Lifetime;
import io.spbx.webby.db.kv.DbType;
import io.spbx.webby.db.kv.KeyValueSettings;
import io.spbx.webby.db.managed.FlushMode;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.TestingProps;
import io.spbx.webby.testing.ext.EmbeddedRedisExtension;
import io.spbx.webby.testing.ext.SqlDbExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBasics.array;

@Tag("slow") @Tag("integration") @Tag("sql")
public class KeyEventStoreIntegrationTest {
    @RegisterExtension private static final TempDirectoryExtension TEMP_DIRECTORY = TempDirectoryExtension.withCleanup();
    @RegisterExtension private static final EmbeddedRedisExtension REDIS = EmbeddedRedisExtension.ofDefaultPort();
    @RegisterExtension private static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withSavepoints();

    @Test
    public void simple_empty_store() {
        KeyEventStoreFactory factory = Testing.testStartup().getInstance(KeyEventStoreFactory.class);

        EventStoreOptions<Integer, String> options = EventStoreOptions.of("foo", Integer.class, String.class);
        try (KeyEventStore<Integer, String> store = factory.getEventStore(options)) {
            assertStore(store).isCleanState();

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isCleanState();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10 })
    public void simple_append_and_flush(int batchSize) {
        KeyEventStoreFactory factory = setup(batchSize).getInstance(KeyEventStoreFactory.class);

        EventStoreOptions<Integer, String> options = EventStoreOptions.of("foo", Integer.class, String.class);
        try (KeyEventStore<Integer, String> store = factory.getEventStore(options)) {
            assertStore(store).isCleanState();

            store.append(1, "a");
            assertStore(store).isEqualTo(array(1, "a"));

            store.append(2, "b");
            assertStore(store).isEqualTo(array(1, "a"), array(2, "b"));

            store.append(1, "c");
            assertStore(store).isEqualTo(array(1, "a", "c"), array(2, "b"));

            store.append(3, "d");
            assertStore(store).isEqualTo(array(1, "a", "c"), array(2, "b"), array(3, "d"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache().isEqualTo(array(1, "a", "c"), array(2, "b"), array(3, "d"));

            store.append(1, "e");
            assertStore(store).isEqualTo(array(1, "a", "c", "e"), array(2, "b"), array(3, "d"));

            store.append(4, "f");
            assertStore(store).isEqualTo(array(1, "a", "c", "e"), array(2, "b"), array(3, "d"), array(4, "f"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache().isEqualTo(array(1, "a", "c", "e"), array(2, "b"), array(3, "d"), array(4, "f"));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10 })
    public void simple_append_one_key_and_flush(int batchSize) {
        KeyEventStoreFactory factory = setup(batchSize).getInstance(KeyEventStoreFactory.class);

        EventStoreOptions<Integer, String> options = EventStoreOptions.of("foo", Integer.class, String.class);
        try (KeyEventStore<Integer, String> store = factory.getEventStore(options)) {
            assertStore(store).isCleanState();

            store.append(1, "a");
            assertStore(store).isEqualTo(array(1, "a"));

            store.append(1, "b");
            assertStore(store).isEqualTo(array(1, "a", "b"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache().isEqualTo(array(1, "a", "b"));

            store.append(1, "c");
            assertStore(store).isEqualTo(array(1, "a", "b", "c"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache().isEqualTo(array(1, "a", "b", "c"));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void simple_operations(DbType dbType) {
        KeyEventStoreFactory factory = setup(dbType).getInstance(KeyEventStoreFactory.class);

        EventStoreOptions<Integer, String> options = EventStoreOptions.of("ops", Integer.class, String.class);
        try (KeyEventStore<Integer, String> store = factory.getEventStore(options)) {
            assertStore(store).isCleanState();

            store.append(1, "a");
            assertStore(store).isEqualTo(array(1, "a"));

            store.append(2, "b");
            assertStore(store).isEqualTo(array(1, "a"), array(2, "b"));

            store.append(1, "c");
            assertStore(store).isEqualTo(array(1, "a", "c"), array(2, "b"));

            store.deleteAll(1);
            assertStore(store).isEqualTo(array(2, "b"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache();
            assertStore(store).isEqualTo(array(2, "b"));

            store.append(1, "e");
            assertStore(store).isEqualTo(array(1, "e"), array(2, "b"));

            store.deleteAll(1);
            assertStore(store).isEqualTo(array(2, "b"));

            store.flush(FlushMode.FULL_CLEAR);
            assertStore(store).isEmptyCache();
            assertStore(store).isEqualTo(array(2, "b"));
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void many_events_per_key(DbType dbType) {
        KeyEventStoreFactory factory = setup(dbType).getInstance(KeyEventStoreFactory.class);

        int maxSize = dbType == DbType.SQL_DB && SQL.engine().isOneOf(Engine.MySQL, Engine.MariaDB) ? 2000 : Integer.MAX_VALUE;
        int size = Math.min(100000, maxSize);
        int flushEvery = size / 10;

        EventStoreOptions<Integer, String> options = EventStoreOptions.of("many", Integer.class, String.class);
        try (KeyEventStore<Integer, String> store = factory.getEventStore(options)) {
            assertStore(store).isCleanState();

            for (int i = 1; i <= size; i++) {
                store.append(1, "12345678");
                if (i % flushEvery == 0) {
                    store.flush(FlushMode.FULL_CLEAR);
                    assertStore(store).isEmptyCache();
                }
            }

            Multimap<Integer, String> expected = new ImmutableMultimap.Builder<Integer, String>()
                .putAll(1, Collections.nCopies(size, "12345678"))
                .build();
            assertStore(store).isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @EnumSource(DbType.class)
    public void lifetime_and_persistence(DbType dbType) {
        Injector injector = setup(dbType);
        Lifetime.Definition lifetime = injector.getInstance(AppLifetime.class).getLifetime();
        KeyEventStoreFactory factory = injector.getInstance(KeyEventStoreFactory.class);

        KeyEventStore<Integer, String> store = factory.getEventStore(
            EventStoreOptions.of("life", Integer.class, String.class)
        );

        assertStore(store).isCleanState();
        store.append(1, "a");
        store.append(2, "b");
        store.append(3, "c");

        lifetime.terminate();
        assertStore(store).isEmptyCache();
    }

    private static @NotNull Injector setup(int batchSize) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.setInt("db.event.store.flush.batch.size", batchSize);
        return Testing.testStartup(settings);
    }

    private static @NotNull Injector setup(@NotNull DbType dbType) {
        TestingProps.assumePropIfSet("test.kv.only_type", dbType.name());

        AppSettings settings = Testing.defaultAppSettings();
        settings.setModelFilter(ClassFilter.ofSelectedPackagesOnly(Testing.CORE_MODELS));
        settings.updateStorageSettings(
            storage -> storage
                .withKeyValue(KeyValueSettings.of(dbType, TEMP_DIRECTORY.getCurrentTempDir()))
                .enableSql(SQL.settings())
        );
        settings.setProfileMode(Toggle.DISABLED);  // not testing TrackingDbAdapter by default

        settings.setInt("db.event.store.flush.batch.size", 1);
        settings.setInt("db.event.store.average.size", 80);

        settings.setInt("db.redis.port", REDIS.port());
        return Testing.testStartup(settings, SQL::setupTestData, SQL.combinedTestingModule());
    }

    @CanIgnoreReturnValue
    private static <K, V> @NotNull KeyEventStoreSubject<K, V> assertStore(@NotNull KeyEventStore<K, V> store) {
        return new KeyEventStoreSubject<>(store);
    }

    @CanIgnoreReturnValue
    private record KeyEventStoreSubject<K, V>(@NotNull KeyEventStore<K, V> store) {
        public @NotNull KeyEventStoreSubject<K, V> isEqualTo(@NotNull Multimap<K, V> expected) {
            for (K key : expected.keySet()) {
                assertThat(store.getAll(key)).containsExactlyElementsIn(expected.get(key));
            }
            return this;
        }

        public @NotNull KeyEventStoreSubject<K, V> isEqualTo(@NotNull Object[] @NotNull ... entries) {
            return isEqualTo(multi(entries));
        }

        public @NotNull KeyEventStoreSubject<K, V> isCleanState() {
            return isEmptyCache().isUnderlyingDbEmpty();
        }

        public @NotNull KeyEventStoreSubject<K, V> isEmptyCache() {
            if (store instanceof CachingKvdbEventStore<?, ?> cachingStore) {
                assertThat(cachingStore.cache()).isEmpty();
            }
            return this;
        }

        public @NotNull KeyEventStoreSubject<K, V> isUnderlyingDbEmpty() {
            if (store instanceof CachingKvdbEventStore<?, ?> cachingStore) {
                assertThat(cachingStore.db().isEmpty()).isTrue();
                assertThat(cachingStore.db().keys()).isEmpty();
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        private static <K, V> @NotNull Multimap<K, V> multi(@NotNull Object[] @NotNull ... entries) {
            Multimap<K, V> result = ArrayListMultimap.create();
            for (Object[] entry : entries) {
                assert entry.length > 0;
                result.putAll((K) entry[0], Arrays.stream(entry).skip(1).map(i -> (V) i).toList());
            }
            return result;
        }
    }
}
