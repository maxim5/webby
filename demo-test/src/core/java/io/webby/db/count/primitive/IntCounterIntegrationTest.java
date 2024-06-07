package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.eventbus.EventBus;
import io.webby.db.StorageType;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.demo.model.UserRateModelTable;
import io.webby.testing.ext.SqlDbExtension;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.content_id;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.user_id;
import static io.webby.testing.AssertPrimitives.assertMap;
import static io.webby.testing.TestingPrimitives.newIntMap;

// FIX[minor]: more test cases (existing state, check group by count, flush)
@Tag("integration") @Tag("sql")
public class IntCounterIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserRateModelTable.META);

    private IntCountStorage storage;
    private IntCounter counter;

    private static final int A = 1000;
    private static final int B = 2000;
    private static final int C = 3000;

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_counts(Scenario scenario) {
        setup(scenario);
        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_simple_inc_dec(Scenario scenario) {
        setup(scenario);
        assertThat(counter.increment(A)).isEqualTo(1);
        assertThat(counter.decrement(B)).isEqualTo(-1);
        assertCounter(counter).hasCountEstimates(A, 1, B, -1, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_simple_update(Scenario scenario) {
        setup(scenario);
        assertThat(counter.update(A, 7)).isEqualTo(7);
        assertCounter(counter).hasCountEstimates(A, 7, B, 0, C, 0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_multiple_updates(Scenario scenario) {
        setup(scenario);
        assertThat(counter.update(A, 5)).isEqualTo(5);
        assertThat(counter.update(B, -3)).isEqualTo(-3);
        assertThat(counter.update(C, 0)).isEqualTo(0);
        assertCounter(counter).hasCountEstimates(A, 5, B, -3, C, 0);

        assertThat(counter.update(A, 2)).isEqualTo(7);
        assertThat(counter.update(B, 1)).isEqualTo(-2);
        assertThat(counter.update(C, -1)).isEqualTo(-1);
        assertCounter(counter).hasCountEstimates(A, 7, B, -2, C, -1);
    }

    private static @NotNull IntCounterSubject assertCounter(@NotNull IntCounter counter) {
        return new IntCounterSubject(counter);
    }

    private record IntCounterSubject(@NotNull IntCounter counter) {
        public void hasCountEstimates(int... expected) {
            IntIntHashMap expectedMap = newIntMap(expected);

            assertMap(counter.estimateCounts(expectedMap.keys())).trimmed().containsExactlyTrimmed(expected);
            for (IntIntCursor cursor : expectedMap) {
                assertThat(counter.estimateCount(cursor.key)).isEqualTo(cursor.value);
            }
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
