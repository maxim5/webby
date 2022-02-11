package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.eventbus.EventBus;
import io.webby.db.StorageType;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.demo.model.UserRateModelTable;
import io.webby.testing.ext.SqlCleanupExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.webby.demo.model.UserRateModelTable.OwnColumn.content_id;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.user_id;
import static io.webby.testing.AssertPrimitives.assertIntsTrimmed;
import static io.webby.testing.TestingPrimitives.newIntMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

// FIX[minor]: more test cases (existing state, check group by count, flush)
@Tag("sql")
public class IntCounterIntegrationTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties().disableSavepoints();
    @RegisterExtension private static final SqlCleanupExtension CLEANUP = SqlCleanupExtension.of(SQL, UserRateModelTable.META);

    private IntCountStorage storage;
    private IntCounter counter;

    private static final int A = 1000;
    private static final int B = 2000;
    private static final int C = 3000;

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_counts(Scenario scenario) {
        setup(scenario);
        assertCountEstimates(A, 0, B, 0, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_simple_inc_dec(Scenario scenario) {
        setup(scenario);
        assertEquals(1, counter.increment(A));
        assertEquals(-1, counter.decrement(B));
        assertCountEstimates(A, 1, B, -1, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_simple_update(Scenario scenario) {
        setup(scenario);
        assertEquals(7, counter.update(A, 7));
        assertCountEstimates(A, 7, B, 0, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_multiple_updates(Scenario scenario) {
        setup(scenario);
        assertEquals(5, counter.update(A, 5));
        assertEquals(-3, counter.update(B, -3));
        assertEquals(0, counter.update(C, 0));
        assertCountEstimates(A, 5, B, -3, C, 0);

        assertEquals(7, counter.update(A, 2));
        assertEquals(-2, counter.update(B, 1));
        assertEquals(-1, counter.update(C, -1));
        assertCountEstimates(A, 7, B, -2, C, -1);
    }

    private void assertCountEstimates(int... expected) {
        IntIntHashMap expectedMap = newIntMap(expected);

        assertIntsTrimmed(counter.estimateCounts(expectedMap.keys()), expected);
        for (IntIntCursor cursor : expectedMap) {
            assertEquals(cursor.value, counter.estimateCount(cursor.key));
        }
    }

    private void setup(@NotNull Scenario scenario) {
        storage = switch (scenario.store) {
            case SQL_DB -> new TableCountStorage(new UserRateModelTable(SQL), content_id, OneOf.ofSecond(user_id));
            case KEY_VALUE_DB -> new KvCountStorage(new JavaMapDbFactory().inMemoryDb());
        };
        counter = new LockBasedIntCounter(storage, new EventBus());
    }

    private enum Scenario {
        TABLE_LOCK(StorageType.SQL_DB),
        KV_JAVA_LOCK(StorageType.KEY_VALUE_DB);

        private final StorageType store;

        Scenario(@NotNull StorageType store) {
            this.store = store;
        }
    }
}
