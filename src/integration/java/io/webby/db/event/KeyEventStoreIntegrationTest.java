package io.webby.db.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.model.BlobKv;
import io.webby.testing.Testing;
import io.webby.testing.TestingProps;
import io.webby.testing.ext.EmbeddedRedisExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.testing.ext.TempDirectoryExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingUtil.array;

@Tags({@Tag("sql"), @Tag("slow")})
public class KeyEventStoreIntegrationTest {
    @RegisterExtension private static final TempDirectoryExtension TEMP_DIRECTORY = new TempDirectoryExtension();
    @RegisterExtension private static final EmbeddedRedisExtension REDIS = new EmbeddedRedisExtension();
    @RegisterExtension private static final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    @Test
    public void simple_empty_store() {
        KeyEventStoreFactory factory = Testing.testStartup().getInstance(KeyEventStoreFactory.class);

        try (KeyEventStore<Integer, String> store = factory.getEventStore("foo", Integer.class, String.class)) {
            assertEmptyCache(store);
            assertEqualsTo(store, multi());

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    public void simple_append_and_flush(int batchSize) {
        KeyEventStoreFactory factory = setup(batchSize).getInstance(KeyEventStoreFactory.class);

        try (KeyEventStore<Integer, String> store = factory.getEventStore("foo", Integer.class, String.class)) {
            store.append(1, "a");
            assertEqualsTo(store, multi(array(1, "a")));

            store.append(2, "b");
            assertEqualsTo(store, multi(array(1, "a"), array(2, "b")));

            store.append(1, "c");
            assertEqualsTo(store, multi(array(1, "a", "c"), array(2, "b")));

            store.append(3, "d");
            assertEqualsTo(store, multi(array(1, "a", "c"), array(2, "b"), array(3, "d")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(1, "a", "c"), array(2, "b"), array(3, "d")));

            store.append(1, "e");
            assertEqualsTo(store, multi(array(1, "a", "c", "e"), array(2, "b"), array(3, "d")));

            store.append(4, "f");
            assertEqualsTo(store, multi(array(1, "a", "c", "e"), array(2, "b"), array(3, "d"), array(4, "f")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(1, "a", "c", "e"), array(2, "b"), array(3, "d"), array(4, "f")));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    public void simple_append_one_key_and_flush(int batchSize) {
        KeyEventStoreFactory factory = setup(batchSize).getInstance(KeyEventStoreFactory.class);

        try (KeyEventStore<Integer, String> store = factory.getEventStore("foo", Integer.class, String.class)) {
            store.append(1, "a");
            assertEqualsTo(store, multi(array(1, "a")));

            store.append(1, "b");
            assertEqualsTo(store, multi(array(1, "a", "b")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(1, "a", "b")));

            store.append(1, "c");
            assertEqualsTo(store, multi(array(1, "a", "b", "c")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(1, "a", "b", "c")));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void simple_operations(StorageType storageType) {
        KeyEventStoreFactory factory = setup(storageType).getInstance(KeyEventStoreFactory.class);

        try (KeyEventStore<Integer, String> store = factory.getEventStore("foo", Integer.class, String.class)) {
            store.append(1, "a");
            assertEqualsTo(store, multi(array(1, "a")));

            store.append(2, "b");
            assertEqualsTo(store, multi(array(1, "a"), array(2, "b")));

            store.append(1, "c");
            assertEqualsTo(store, multi(array(1, "a", "c"), array(2, "b")));

            store.deleteAll(1);
            assertEqualsTo(store, multi(array(2, "b")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(2, "b")));

            store.append(1, "e");
            assertEqualsTo(store, multi(array(1, "e"), array(2, "b")));

            store.deleteAll(1);
            assertEqualsTo(store, multi(array(2, "b")));

            store.forceFlush();
            assertEmptyCache(store);
            assertEqualsTo(store, multi(array(2, "b")));
        }
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    public void many_events_per_key(StorageType storageType) {
        KeyEventStoreFactory factory = setup(storageType).getInstance(KeyEventStoreFactory.class);

        int size = 100000;
        int flushEvery = 10000;
        try (KeyEventStore<Integer, String> store = factory.getEventStore("foo", Integer.class, String.class)) {
            for (int i = 1; i <= size; i++) {
                store.append(1, "12345678");
                if (i % flushEvery == 0) {
                    store.forceFlush();
                    assertEmptyCache(store);
                }
            }

            Multimap<Integer, String> expected = new ImmutableMultimap.Builder<Integer, String>()
                    .putAll(1, Collections.nCopies(size, "12345678"))
                    .build();
            assertEqualsTo(store, expected);
        }
    }

    private static @NotNull Injector setup(int batchSize) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.setProperty("db.event.store.flush.batch.size", batchSize);
        return Testing.testStartup(settings);
    }

    private static @NotNull Injector setup(@NotNull StorageType storageType) {
        TestingProps.assumePropIfSet("test.kv.only_type", storageType.name());

        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(Session.class, DefaultUser.class, BlobKv.class);
        settings.storageSettings()
                .enableKeyValue(KeyValueSettings.of(storageType, TEMP_DIRECTORY.getCurrentTempDir()))
                .enableSql(SQL_DB.settings());
        settings.setProfileMode(false);  // not testing TrackingDbAdapter by default

        settings.setProperty("db.event.store.flush.batch.size", 1);
        settings.setProperty("db.event.store.average.size", 80);

        settings.setProperty("db.redis.port", REDIS.getPort());
        return Testing.testStartup(settings, SQL_DB::savepoint, SQL_DB.combinedTestingModule());
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

    private static <K, V> void assertEqualsTo(@NotNull KeyEventStore<K, V> store, @NotNull Multimap<K, V> expected) {
        for (K key : expected.keySet()) {
            assertThat(store.getAll(key)).containsExactlyElementsIn(expected.get(key));
        }
    }

    private static <K, V> void assertEmptyCache(@NotNull KeyEventStore<K, V> store) {
        if (store instanceof CachingKvdbEventStore<?, ?> cachingStore) {
            assertThat(cachingStore.cache()).isEmpty();
        }
    }
}
