package io.webby.db.count.impl;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.demo.model.UserRateModel;
import io.webby.demo.model.UserRateModelTable;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.assertIntsNoOrder;
import static io.webby.testing.TestingPrimitives.ints;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("sql")
public class TableVotingStorageIntegrationTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    private static final int A = 1000;
    private static final int B = 2000;

    private static final int Ann = 10;
    private static final int Bob = 20;
    private static final int Don = 30;

    private TableVotingStorage storage;
    private UserRateModelTable table;

    @BeforeAll
    static void beforeAll() {
        SQL_DB.runUpdate("DROP TABLE IF EXISTS %s".formatted(UserRateModelTable.META.sqlTableName()));
        SQL_DB.runUpdate(SqlSchemaMaker.makeCreateTableQuery(SQL_DB.engine(), UserRateModelTable.META));
    }

    @BeforeEach
    void setUp() {
        table = new UserRateModelTable(SQL_DB);
        storage = TableVotingStorage.from(new UserRateModelTable(SQL_DB), "content_id", "user_id", "value");
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
        assertEquals(state, newIntObjectMap(A, IntHashSet.from(Ann, -Bob)));

        assertIntsNoOrder(storage.load(A), Ann, -Bob);
        assertIntsNoOrder(storage.load(B));
        assertEquals(storage.loadBatch(IntArrayList.from(A, B)), state);
        assertEquals(storage.loadAll(), newIntObjectMap(A, IntHashSet.from(Ann, -Bob)));
    }

    @Test
    public void load_two_keys() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(B, Bob, -1));
        assertEquals(state, newIntObjectMap(A, IntHashSet.from(Ann), B, IntHashSet.from(-Bob)));

        assertIntsNoOrder(storage.load(A), Ann);
        assertIntsNoOrder(storage.load(B), -Bob);
        assertEquals(storage.loadBatch(IntArrayList.from(A, B)), state);
        assertEquals(storage.loadAll(), state);
    }

    @Test
    public void store_batch_empty_insert_one_key() {
        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann, Bob, Don)),
                           newIntObjectMap());
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, A, 1),
                                                     new UserRateModel(Don, A, 1));
    }

    @Test
    public void store_batch_empty_insert_two_keys() {
        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann), B, IntHashSet.from(-Ann)),
                           newIntObjectMap());
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Ann, B, -1));
    }

    @Test
    public void store_batch_one_key_insert_new_key() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann), B, IntHashSet.from(Bob)),
                           state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, B, 1));
    }

    @Test
    public void store_batch_one_key_delete() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1));

        storage.storeBatch(newIntObjectMap(),
                           state);
        assertThat(table.fetchAll()).containsExactly();
    }

    @Test
    public void store_batch_two_keys_flip_sign() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(B, Bob, -1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann), B, IntHashSet.from(Bob)),
                           state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1),
                                                     new UserRateModel(Bob, B, 1));
    }

    @Test
    public void store_batch_insert_and_update() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(A, Bob, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(Ann, -Bob, Don)),
                           state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, 1),
                                                     new UserRateModel(Bob, A, -1),
                                                     new UserRateModel(Don, A, 1));
    }

    @Test
    public void store_batch_insert_update_delete() {
        IntObjectMap<IntHashSet> state = setupTestData(ints(A, Ann, 1),
                                                       ints(A, Bob, 1));

        storage.storeBatch(newIntObjectMap(A, IntHashSet.from(-Ann), B, IntHashSet.from(Bob)),
                           state);
        assertThat(table.fetchAll()).containsExactly(new UserRateModel(Ann, A, -1),
                                                     new UserRateModel(Bob, B, 1));
    }

    // TODO[norm]: test cases when DB changed (fallback)

    @CanIgnoreReturnValue
    private @NotNull IntObjectMap<IntHashSet> setupTestData(int[] @NotNull ... rows) {
        List<UserRateModel> models = Arrays.stream(rows).map(row -> new UserRateModel(row[1], row[0], row[2])).toList();
        table.insertBatch(models);

        return rowsToMap(rows);
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
