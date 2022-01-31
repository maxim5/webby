package io.webby.db.count.vote;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.google.common.eventbus.EventBus;
import com.google.mu.util.stream.BiStream;
import io.webby.db.StorageType;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.demo.model.UserRateModelTable;
import io.webby.testing.ext.SqlCleanupExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.webby.db.count.vote.Vote.none;
import static io.webby.db.count.vote.Vote.votes;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.*;
import static io.webby.testing.AssertPrimitives.assertInts;
import static io.webby.testing.AssertPrimitives.assertIntsTrimmed;
import static io.webby.testing.TestingPrimitives.newIntMap;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("sql")
public class VotingCounterIntegrationTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties().disableSavepoints();
    @RegisterExtension private static final SqlCleanupExtension CLEANUP = SqlCleanupExtension.of(SQL, UserRateModelTable.META);

    private static final int A = 1000;
    private static final int B = 2000;
    private static final int C = 3000;

    private static final int Ann = 1;
    private static final int Bob = 2;
    private static final int Liz = 99;

    private VotingCounter counter;
    private VotingStorage storage;
    private EventBus eventBus;

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_counts(Scenario scenario) {
        setup(scenario);

        assertEquals(0, counter.estimateCount(A));
        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_flush(Scenario scenario) throws IOException {
        setup(scenario);

        counter.flush();

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_simple(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));

        assertCountEstimates(A, 1, B, 0, C, 0);
        assertActorValues(Ann, votes(+A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_double(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));

        assertCountEstimates(A, 1, B, 0, C, 0);
        assertActorValues(Ann, votes(+A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_simple(Scenario scenario) {
        setup(scenario);

        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1, B, 0, C, 0);
        assertActorValues(Ann, votes(-A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_double(Scenario scenario) {
        setup(scenario);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1, B, 0, C, 0);
        assertActorValues(Ann, votes(-A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_dec_same_key(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(0, counter.decrement(A, Ann));

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_multi_inc_dec_same_key(Scenario scenario) {
        setup(scenario);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(0, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(0, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_different_keys(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Ann));
        assertEquals(1, counter.increment(C, Ann));

        assertCountEstimates(A, 1, B, 1, C, 1);
        assertActorValues(Ann, votes(+A, +B, +C));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_same_key(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(2, counter.increment(A, Bob));
        assertEquals(3, counter.increment(A, Liz));

        assertCountEstimates(A, 3, B, 0, C, 0);
        assertActorValues(Ann, votes(+A), Bob, votes(+A), Liz, votes(+A));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_dec_same_key(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(2, counter.increment(A, Bob));
        assertEquals(3, counter.increment(A, Liz));
        assertEquals(2, counter.decrement(A, Ann));
        assertEquals(1, counter.decrement(A, Bob));
        assertEquals(0, counter.decrement(A, Liz));

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A)), Bob, votes(none(A)), Liz, votes(none(A)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_users_inc_dec_different_key(Scenario scenario) {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(0, counter.decrement(A, Bob));
        assertEquals(1, counter.increment(B, Liz));
        assertEquals(0, counter.decrement(B, Ann));
        assertEquals(1, counter.increment(C, Bob));
        assertEquals(2, counter.increment(C, Liz));

        assertCountEstimates(A, 0, B, 0, C, 2);
        assertActorValues(Ann, votes(+A, -B, none(C)),
                          Bob, votes(-A, +C, none(B)),
                          Liz, votes(+B, +C, none(A)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_inc_flush(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Bob));
        assertEquals(2, counter.increment(A, Liz));
        counter.flush();

        assertCountEstimates(A, 2, B, 1);
        assertActorValues(Ann, votes(+A), Bob, votes(+B), Liz, votes(+A));
        assertStorage(A, IntHashSet.from(+Ann, +Liz),
                      B, IntHashSet.from(+Bob));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_dec_flush(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(B, Bob));
        assertEquals(-2, counter.decrement(A, Liz));
        counter.flush();

        assertCountEstimates(A, -2, B, -1);
        assertActorValues(Ann, votes(-A), Bob, votes(-B), Liz, votes(-A));
        assertStorage(A, IntHashSet.from(-Ann, -Liz),
                      B, IntHashSet.from(-Bob));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_double_flush(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Ann));
        counter.flush();
        counter.flush();

        assertCountEstimates(A, 1, B, 1);
        assertActorValues(Ann, votes(+A, +B));
        assertStorage(A, IntHashSet.from(+Ann),
                      B, IntHashSet.from(+Ann));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(1, counter.increment(B, Ann));
        counter.flush();

        assertCountEstimates(A, 1, B, 1);
        assertActorValues(Ann, votes(+A, +B));
        assertStorage(A, IntHashSet.from(+Ann),
                      B, IntHashSet.from(+Ann));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_inc_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(0, counter.decrement(A, Ann));
        counter.flush();

        assertCountEstimates(A, 0);
        assertActorValues(Ann, votes(none(A)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_dec_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(-1, counter.decrement(A, Ann));
        counter.flush();
        assertEquals(0, counter.increment(A, Ann));
        counter.flush();

        assertCountEstimates(A, 0);
        assertActorValues(Ann, votes(none(A)));
        assertStorage();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_flip_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(0, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));
        counter.flush();

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(A, IntHashSet.from(-Ann));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_no_change_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();

        assertCountEstimates(A, 1, B, -1);
        assertActorValues(Ann, votes(+A, -B));
        assertStorage(A, IntHashSet.from(+Ann),
                      B, IntHashSet.from(-Ann));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_changes_between_flushes(Scenario scenario) throws IOException {
        setup(scenario);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();
        assertEquals(-1, counter.decrement(C, Ann));
        assertEquals(0, counter.increment(B, Bob));
        counter.flush();

        assertCountEstimates(A, 1, B, 0, C, -1);
        assertActorValues(Ann, votes(+A, -B, -C),
                          Bob, votes(+B));
        assertStorage(A, IntHashSet.from(Ann),
                      B, IntHashSet.from(-Ann, +Bob),
                      C, IntHashSet.from(-Ann));
    }

    // TODO[!]: more flush test cases

    private void assertCountEstimates(int... expected) {
        IntIntHashMap expectedMap = newIntMap(expected);

        assertIntsTrimmed(counter.estimateCounts(expectedMap.keys), expected);
        for (IntIntCursor cursor : expectedMap) {
            assertEquals(cursor.value, counter.estimateCount(cursor.key));
        }
    }

    private void assertActorValues(@NotNull Object @NotNull ... expected) {
        IntObjectMap<List<Vote>> expectedMap = newIntObjectMap(expected);

        for (IntObjectCursor<List<Vote>> cursor : expectedMap) {
            int user = cursor.key;
            for (Vote vote : cursor.value) {
                assertEquals(vote.val(), counter.getVote(vote.key(), user), "user=%d expected=%s".formatted(user, vote));
                assertInts(counter.getVotes(IntArrayList.from(vote.key()), user), vote.key(), vote.val());
            }

            IntArrayList keys = EasyHppc.fromJavaIterableInt(cursor.value.stream().map(Vote::key).toList());
            Map<Integer, Integer> expectedActorValues =
                BiStream.biStream(cursor.value.stream()).mapKeys(Vote::key).mapValues(Vote::val).toMap();
            assertInts(counter.getVotes(keys, user), expectedActorValues);
        }
    }

    private void assertStorage(@NotNull Object @NotNull ... expected) {
        IntObjectMap<IntHashSet> expectedMap = newIntObjectMap(expected);
        assertEquals(expectedMap, storage.loadAll());
    }

    private void setup(@NotNull Scenario scenario) {
        eventBus = new EventBus();

        storage = switch (scenario.store) {
            case SQL_DB -> new TableVotingStorage(new UserRateModelTable(SQL), content_id, user_id, value);
            case KEY_VALUE_DB -> new KvVotingStorage("java", new JavaMapDbFactory().inMemoryDb());
        };

        counter = switch (scenario.counter) {
            case LOCK_BASED -> new LockBasedVotingCounter(storage, eventBus);
            case NON_BLOCKING -> new NonBlockingVotingCounter(storage, eventBus);
        };
    }

    private enum Scenario {
        TABLE_LOCK(StorageType.SQL_DB, VotingCounterType.LOCK_BASED),
        TABLE_NB(StorageType.SQL_DB, VotingCounterType.NON_BLOCKING),
        KV_JAVA_LOCK(StorageType.KEY_VALUE_DB, VotingCounterType.LOCK_BASED),
        KV_JAVA_NB(StorageType.KEY_VALUE_DB, VotingCounterType.NON_BLOCKING);

        private final StorageType store;
        private final VotingCounterType counter;

        Scenario(@NotNull StorageType store, @NotNull VotingCounterType counter) {
            this.store = store;
            this.counter = counter;
        }
    }
}
