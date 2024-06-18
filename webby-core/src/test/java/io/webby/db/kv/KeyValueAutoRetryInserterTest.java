package io.webby.db.kv;

import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.testing.db.model.FakeIntIdGenerator;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.collect.EasyMaps.asMap;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class KeyValueAutoRetryInserterTest {
    private final FakeIntIdGenerator generator = FakeIntIdGenerator.range(1, 5);
    private KeyValueDb<Integer, String> keyValueDb;

    @Test
    public void first_attempt_success() {
        keyValueDb = newKeyValueDb();
        KeyValueAutoRetryInserter<Integer, String> inserter = new KeyValueAutoRetryInserter<>(keyValueDb, generator, 1);
        Pair<Integer, String> inserted = inserter.insertOrDie(key -> "value-" + key);
        assertThat(inserted).isEqualTo(Pair.of(1, "value-1"));
        assertThat(keyValueDb.asMap()).containsExactly(1, "value-1");
    }

    @Test
    public void second_attempt_success() {
        keyValueDb = newKeyValueDb(1, "foo");
        KeyValueAutoRetryInserter<Integer, String> inserter = new KeyValueAutoRetryInserter<>(keyValueDb, generator, 3);
        Pair<Integer, String> inserted = inserter.insertOrDie(key -> "value-" + key);
        assertThat(inserted).isEqualTo(Pair.of(2, "value-2"));
        assertThat(keyValueDb.asMap()).containsExactly(1, "foo", 2, "value-2");
    }

    @Test
    public void last_attempt_success() {
        keyValueDb = newKeyValueDb(1, "foo", 2, "bar");
        KeyValueAutoRetryInserter<Integer, String> inserter = new KeyValueAutoRetryInserter<>(keyValueDb, generator, 3);
        Pair<Integer, String> inserted = inserter.insertOrDie(key -> "value-" + key);
        assertThat(inserted).isEqualTo(Pair.of(3, "value-3"));
        assertThat(keyValueDb.asMap()).containsExactly(1, "foo", 2, "bar", 3, "value-3");
    }

    @Test
    public void all_attempts_failed() {
        keyValueDb = newKeyValueDb(1, "foo", 2, "bar", 3, "baz");
        KeyValueAutoRetryInserter<Integer, String> inserter = new KeyValueAutoRetryInserter<>(keyValueDb, generator, 3);
        assertThrows(Exception.class, () -> inserter.insertOrDie(key -> "value-" + key));
    }

    private @NotNull static KeyValueDb<Integer, String> newKeyValueDb(@NotNull Object ... objects) {
        return new JavaMapDbFactory().inMemoryDb(asMap(objects));
    }
}
