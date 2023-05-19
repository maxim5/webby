package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.demo.model.UserRateModel;
import io.webby.demo.model.UserRateModelTable;
import io.webby.orm.api.query.Shortcuts;
import io.webby.orm.api.query.Where;
import io.webby.testing.ext.FluentLoggingCapture;
import io.webby.testing.ext.SqlDbExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.*;
import static io.webby.testing.AssertPrimitives.assertMap;
import static io.webby.testing.AssertPrimitives.assertThat;
import static io.webby.testing.TestingPrimitives.ints;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;

@Tag("sql")
public class TableVotingStorageIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserRateModelTable.META);
    @RegisterExtension static final FluentLoggingCapture LOGGING = new FluentLoggingCapture(Consistency.class);

    private static final int A = 1000;
    private static final int B = 2000;

    private static final int Ann = 10;
    private static final int Bob = 20;
    private static final int Don = 30;

    private TableVotingStorage storage;
    private UserRateModelTable table;

    @BeforeEach
    void setUp() {
        table = new UserRateModelTable(SQL);
        storage = new TableVotingStorage(new UserRateModelTable(SQL), content_id, user_id, value);
    }

    @Test
    public void load_empty() {
        assertThat(storage.load(A)).isEmpty();
        assertThat(storage.loadBatch(IntArrayList.from())).isEmpty();
        assertThat(storage.loadBatch(IntArrayList.from(A, B))).isEmpty();
        assertThat(storage.loadAll()).isEmpty();
    }

    @Test
    public void load_one_key() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(A, Bob, -1));
        assertMap(state).containsExactly(A, IntHashSet.from(Ann, -Bob));

        assertThat(storage.load(A)).containsExactlyNoOrder(Ann, -Bob);
        assertThat(storage.load(B)).containsExactlyNoOrder();
        assertMap(storage.loadBatch(IntArrayList.from(A, B))).isEqualTo(state);
        assertMap(storage.loadAll()).isEqualTo(newIntObjectMap(A, IntHashSet.from(Ann, -Bob)));
    }

    @Test
    public void load_two_keys() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(B, Bob, -1));
        assertMap(state).containsExactly(A, IntHashSet.from(Ann), B, IntHashSet.from(-Bob));

        assertThat(storage.load(A)).containsExactlyNoOrder(Ann);
        assertThat(storage.load(B)).containsExactlyNoOrder(-Bob);
        assertMap(storage.loadBatch(IntArrayList.from(A, B))).isEqualTo(state);
        assertMap(storage.loadAll()).isEqualTo(state);
    }

    @Test
    public void store_batch_empty_insert_one_key() {
        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann, Bob, Don)),
                           newIntObjectMap());
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, A, 1),
                                                     new UserRateModel(Don, A, 1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_empty_insert_two_keys() {
        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann), B, IntHashSet.from(-Ann)),
                           newIntObjectMap());
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Ann, B, -1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_one_key_insert_new_key() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann), B, IntHashSet.from(Bob)), state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, B, 1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_one_key_delete() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from()), state);
        assertThat(table.fetchAll()).isEmpty();
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_one_key_insert_and_update() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(A, Bob, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann, -Bob, Don)), state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, A, -1),
                                                     new UserRateModel(Don, A, 1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_two_keys_flip_sign() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(B, Bob, -1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann), B, IntHashSet.from(Bob)), state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1),
                                                     new UserRateModel(Bob, B, 1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_two_keys_insert_update_delete() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(A, Bob, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann), B, IntHashSet.from(Bob)), state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1),
                                                     new UserRateModel(Bob, B, 1));
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_two_keys_delete_all() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(B, Bob, -1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(), B, IntHashSet.from()), state);
        assertThat(table.fetchAll()).isEmpty();
        LOGGING.assertNoRecords();
    }

    @Test
    public void store_batch_out_of_sync_one_key_changed_cache_overwrites() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));
        overwriteTestData(ints(A, Ann, -1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann)), state);
        // assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1));     // overwrites
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[-10] removed=[10]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_changed_cache_matches() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));
        overwriteTestData(ints(A, Ann, -1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann)), state);
        // assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1));    // same as before
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[-10] removed=[10]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_deleted_cache_overwrites() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));
        overwriteTestData();

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann)), state);
        // assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1));    // overwrites
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[] removed=[10]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_deleted_cache_matches() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));
        overwriteTestData();

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from()), state);
        // assertThat(table.fetchAll()).isEmpty();                                         // matches
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[] removed=[10]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_inserted_cache_overwrites() {
        IntObjectMap<IntHashSet> state = setupTestData();
        overwriteTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann)), state);
        // assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1));    // overwrites
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[10] removed=[]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_inserted_cache_matches() {
        IntObjectMap<IntHashSet> state = setupTestData();
        overwriteTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann)), state);
        // assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1));     // matches
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[10] removed=[]")).isNotEmpty();
    }

    @Test
    public void store_batch_out_of_sync_one_key_inserted_cache_deletes() {
        IntObjectMap<IntHashSet> state = setupTestData();
        overwriteTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from()), state);
        // assertThat(table.fetchAll()).isEmpty();                                         // overwrites
        assertThat(LOGGING.logRecordsContaining("key=1000 added=[10] removed=[]")).isNotEmpty();
    }

    @CanIgnoreReturnValue
    private @NotNull IntObjectMap<IntHashSet> setupTestData(int[] @NotNull ... rows) {
        insertRows(rows);
        return rowsToMap(rows);
    }

    private void overwriteTestData(int[] @NotNull ... rows) {
        table.deleteWhere(Where.of(Shortcuts.TRUE));
        insertRows(rows);
    }

    private void insertRows(int[] @NotNull [] rows) {
        List<UserRateModel> models = Arrays.stream(rows).map(row -> new UserRateModel(row[1], row[0], row[2])).toList();
        table.insertBatch(models);
    }

    private static @NotNull IntObjectHashMap<IntHashSet> rowsToMap(int[] @NotNull [] rows) {
        IntObjectHashMap<IntHashSet> map = new IntObjectHashMap<>();
        for (int[] row : rows) {
            map.putIfAbsent(row[0], new IntHashSet());
            map.get(row[0]).add(row[1] * row[2]);
        }
        return map;
    }
}
