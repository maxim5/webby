package io.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.google.common.collect.HashBasedTable;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;
import com.google.mu.util.stream.BiStream;
import io.webby.db.StorageType;
import io.webby.db.count.StoreChangedEvent;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.demo.model.UserRateModelTable;
import io.webby.testing.ext.SqlCleanupExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

import static io.webby.db.count.vote.Vote.none;
import static io.webby.db.count.vote.Vote.votes;
import static io.webby.demo.model.UserRateModelTable.OwnColumn.*;
import static io.webby.testing.AssertPrimitives.assertInts;
import static io.webby.testing.AssertPrimitives.assertIntsTrimmed;
import static io.webby.testing.TestingPrimitives.newIntMap;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;
import static java.util.Objects.requireNonNull;
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
    public void empty_state_counts(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(0, counter.estimateCount(A));
        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_flush(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        counter.flush();

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_simple(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));

        assertCountEstimates(A, 1, B, 0, C, 0);
        assertActorValues(Ann, votes(+A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_double(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));

        assertCountEstimates(A, 1, B, 0, C, 0);
        assertActorValues(Ann, votes(+A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_simple(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1, B, 0, C, 0);
        assertActorValues(Ann, votes(-A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_double(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1, B, 0, C, 0);
        assertActorValues(Ann, votes(-A, none(B), none(C)),
                          Bob, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(0, counter.decrement(A, Ann));

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_multi_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(0, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(0, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_different_keys(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Ann));
        assertEquals(1, counter.increment(C, Ann));

        assertCountEstimates(A, 1, B, 1, C, 1);
        assertActorValues(Ann, votes(+A, +B, +C));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(2, counter.increment(A, Bob));
        assertEquals(3, counter.increment(A, Liz));

        assertCountEstimates(A, 3, B, 0, C, 0);
        assertActorValues(Ann, votes(+A), Bob, votes(+A), Liz, votes(+A));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(2, counter.increment(A, Bob));
        assertEquals(3, counter.increment(A, Liz));
        assertEquals(2, counter.decrement(A, Ann));
        assertEquals(1, counter.decrement(A, Bob));
        assertEquals(0, counter.decrement(A, Liz));

        assertCountEstimates(A, 0, B, 0, C, 0);
        assertActorValues(Ann, votes(none(A)), Bob, votes(none(A)), Liz, votes(none(A)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_users_inc_dec_different_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

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
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_inc_flush(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Bob));
        assertEquals(2, counter.increment(A, Liz));
        counter.flush();

        assertCountEstimates(A, 2, B, 1);
        assertActorValues(Ann, votes(+A), Bob, votes(+B), Liz, votes(+A));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann, +Liz),
                                      B, IntHashSet.from(+Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_dec_flush(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(B, Bob));
        assertEquals(-2, counter.decrement(A, Liz));
        counter.flush();

        assertCountEstimates(A, -2, B, -1);
        assertActorValues(Ann, votes(-A), Bob, votes(-B), Liz, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann, -Liz),
                                      B, IntHashSet.from(-Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_double_flush(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(1, counter.increment(B, Ann));
        counter.flush();
        counter.flush();

        assertCountEstimates(A, 1, B, 1);
        assertActorValues(Ann, votes(+A, +B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann),
                                      B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(1, counter.increment(B, Ann));
        counter.flush();

        assertCountEstimates(A, 1, B, 1);
        assertActorValues(Ann, votes(+A, +B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann),
                                      B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_inc_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(0, counter.decrement(A, Ann));
        counter.flush();

        assertCountEstimates(A, 0);
        assertActorValues(Ann, votes(none(A)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_dec_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));
        counter.flush();
        assertEquals(0, counter.increment(A, Ann));
        counter.flush();

        assertCountEstimates(A, 0);
        assertActorValues(Ann, votes(none(A)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_flip_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        counter.flush();
        assertEquals(0, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));
        counter.flush();

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_no_change_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();
        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();

        assertCountEstimates(A, 1, B, -1);
        assertActorValues(Ann, votes(+A, -B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann),
                                      B, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_changes_between_flushes(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(1, counter.increment(A, Ann));
        assertEquals(-1, counter.decrement(B, Ann));
        counter.flush();
        assertEquals(-1, counter.decrement(C, Ann));
        assertEquals(0, counter.increment(B, Bob));
        counter.flush();

        assertCountEstimates(A, 1, B, 0, C, -1);
        assertActorValues(Ann, votes(+A, -B, -C),
                          Bob, votes(+B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann),
                                      B, IntHashSet.from(-Ann, +Bob),
                                      C, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_load_counts(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(Ann),
                                        B, IntHashSet.from(-Bob)));

        assertCountEstimates(A, 1, B, -1, C, 0);
        assertActorValues(Ann, votes(+A, none(B), none(C)),
                          Bob, votes(none(A), -B, none(C)));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann),
                                      B, IntHashSet.from(-Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_no_changes_flush(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        counter.flush();

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_undo_inc(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(Ann)));

        assertEquals(0, counter.decrement(A, Ann));
        counter.flush();

        assertCountEstimates(A, 0);
        assertActorValues(Ann, votes(none(A)));
        assertStorage(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_flip(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertEquals(0, counter.increment(A, Ann));
        assertEquals(1, counter.increment(A, Ann));
        counter.flush();

        assertCountEstimates(A, 1);
        assertActorValues(Ann, votes(+A));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_new_key_changed(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertEquals(1, counter.increment(B, Ann));
        counter.flush();

        assertCountEstimates(A, -1, B, 1);
        assertActorValues(Ann, votes(-A, +B));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann),
                                      B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_new_user_same_key(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertEquals(-2, counter.decrement(A, Bob));
        counter.flush();

        assertCountEstimates(A, -2);
        assertActorValues(Ann, votes(-A),
                          Bob, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann, -Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_multi_changes_simple(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann),
                                        B, IntHashSet.from(-Bob)));

        assertEquals(2, counter.increment(A, Bob));
        assertEquals(3, counter.increment(A, Liz));
        assertEquals(-2, counter.decrement(B, Ann));
        assertEquals(-3, counter.decrement(B, Liz));
        counter.flush();

        assertCountEstimates(A, 3, B, -3, C, 0);
        assertActorValues(Ann, votes(+A, -B),
                          Bob, votes(+A, -B),
                          Liz, votes(+A, -B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann, +Bob, +Liz),
                                      B, IntHashSet.from(-Ann, -Bob, -Liz)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_multi_changes_various(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann, +Bob),
                                        B, IntHashSet.from(+Ann, +Bob, +Liz)));

        assertEquals(0, counter.decrement(A, Ann));     // double
        assertEquals(-1, counter.decrement(A, Liz));    // new
        assertEquals(-2, counter.decrement(A, Bob));    // undo
        assertEquals(-3, counter.decrement(A, Bob));    // flip
        assertEquals(2, counter.decrement(B, Ann));     // undo
        assertEquals(2, counter.increment(B, Bob));     // double
        assertEquals(1, counter.increment(C, Bob));     // new
        assertEquals(0, counter.decrement(C, Liz));     // new
        counter.flush();

        assertCountEstimates(A, -3, B, 2, C, 0);
        assertActorValues(Ann, votes(-A, none(B), none(C)),
                          Bob, votes(-A, +B, +C),
                          Liz, votes(-A, +B, -C));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann, -Bob, -Liz),
                                      B, IntHashSet.from(+Bob, +Liz),
                                      C, IntHashSet.from(+Bob, -Liz)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_db_row_deleted(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertEquals(0, counter.decrement(A, Ann));
        assertEquals(-1, counter.decrement(A, Ann));    // flipped

        pushToStorage(StorageState.EMPTY);
        counter.flush();

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_db_row_inserted(Scenario scenario) throws IOException {
        setup(scenario, StorageState.EMPTY);

        assertEquals(-1, counter.decrement(A, Ann));

        pushToStorage(StorageState.of(A, IntHashSet.from(+Ann)));
        counter.flush();

        assertCountEstimates(A, -1);
        assertActorValues(Ann, votes(-A));
        assertStorage(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_unrelated_db_row_deleted(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertEquals(-1, counter.decrement(B, Ann));

        pushToStorage(StorageState.EMPTY);
        counter.flush();

        assertCountEstimates(A, 0, B, -1);
        assertActorValues(Ann, votes(none(A), -B));
        assertStorage(StorageState.of(B, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_unrelated_db_row_inserted(Scenario scenario) throws IOException {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertEquals(2, counter.increment(A, Bob));

        pushToStorage(StorageState.of(B, IntHashSet.from(-Bob)));
        counter.flush();

        assertCountEstimates(A, 2, B, -1);
        assertActorValues(Ann, votes(+A),
                          Bob, votes(+A, -B));
        assertStorage(StorageState.of(A, IntHashSet.from(+Ann, +Bob),
                                      B, IntHashSet.from(-Bob)));
    }

    @Tag("slow")
    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void stress_test(Scenario scenario) throws IOException {
        int users = 10;
        int keys = 30;
        int num = 10000;
        double initFlushProb = 0.5, flushProb = 0.5;
        Random random = new Random(0);

        HashBasedTable<Integer, Integer, Integer> expectedVotes = HashBasedTable.create();  // key, user -> {+1, 0, -1}
        setup(scenario, StorageState.EMPTY);

        for (int i = 0; i < num; i++) {
            // cast a vote
            {
                int user = random.nextInt(users) + 1;
                int key = random.nextInt(keys) + 1;
                boolean vote = random.nextBoolean();

                int current = expectedVotes.contains(key, user) ? requireNonNull(expectedVotes.get(key, user)) : 0;
                expectedVotes.put(key, user, Ints.constrainToRange(current + (vote ? 1 : -1), -1, 1));
                int sum = expectedVotes.row(key).values().stream().mapToInt(x -> x).sum();

                if (vote) {
                    assertEquals(sum, counter.increment(key, user));
                } else {
                    assertEquals(sum, counter.decrement(key, user));
                }
            }

            // assert votes
            for (Integer voter : expectedVotes.columnKeySet()) {
                Map<Integer, Integer> expectedColumn = expectedVotes.column(voter);
                IntIntMap actualVotes = counter.getVotes(EasyHppc.fromJavaIterableInt(expectedColumn.keySet()), voter);
                assertInts(actualVotes, expectedColumn);
            }

            // assert counts
            IntIntMap counts = counter.estimateCounts(EasyHppc.fromJavaIterableInt(expectedVotes.rowKeySet()));
            for (IntIntCursor cursor : counts) {
                int sum = expectedVotes.row(cursor.key).values().stream().mapToInt(x -> x).sum();
                assertEquals(sum, cursor.value);
            }

            // (maybe) flush
            if (random.nextDouble() < flushProb) {
                // long start = System.currentTimeMillis();
                counter.flush();
                // long time = System.currentTimeMillis() - start;
                flushProb = flushProb / 2;
                if (flushProb < 0.001) {
                    flushProb = initFlushProb;
                }
                // System.out.println(flushProb + " " + i + " -> " + time + " ms");
            }
        }
    }

    private void assertCountEstimates(int... expected) {
        IntIntHashMap expectedMap = newIntMap(expected);

        assertIntsTrimmed(counter.estimateCounts(expectedMap.keys()), expected);
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

    public void assertStorage(@NotNull StorageState state) {
        assertEquals(state.map(), storage.loadAll());
    }

    public void pushToStorage(@NotNull StorageState state) {
        IntObjectMap<IntHashSet> map = new IntObjectHashMap<>(state.map());
        for (IntObjectCursor<IntHashSet> cursor : storage.loadAll()) {
            map.putIfAbsent(cursor.key, new IntHashSet());
        }

        storage.storeBatch(map, null);  // FIX[minor]: add a dedicated method for testing?
        assertStorage(state);

        eventBus.post(new StoreChangedEvent(storage.storeId()));
    }

    private record StorageState(@NotNull IntObjectMap<IntHashSet> map) {
        public static final StorageState EMPTY = StorageState.of();
        public static StorageState of(@NotNull Object @NotNull ... values) {
            return new StorageState(newIntObjectMap(values));
        }
    }

    private void setup(@NotNull Scenario scenario, @NotNull StorageState state) {
        eventBus = new EventBus();

        storage = switch (scenario.store) {
            case SQL_DB -> new TableVotingStorage(new UserRateModelTable(SQL), content_id, user_id, value);
            case KEY_VALUE_DB -> new KvVotingStorage("java", new JavaMapDbFactory().inMemoryDb(cloneValuesMap()));
        };
        pushToStorage(state);

        counter = switch (scenario.counter) {
            case LOCK_BASED -> new LockBasedVotingCounter(storage, eventBus);
            case NON_BLOCKING -> new NonBlockingVotingCounter(storage, eventBus);
        };
    }

    // Make sure the values are copied.
    // Otherwise, the `VotingCounter` instance can modify the `VotingStorage` state, which will cause mismatch warnings.
    private static @NotNull Map<Integer, IntHashSet> cloneValuesMap() {
        return new HashMap<>() {
            @Override
            public IntHashSet get(Object key) {
                return copy(super.get(key));
            }

            @Override
            public IntHashSet getOrDefault(Object key, IntHashSet defaultValue) {
                return copy(super.getOrDefault(key, defaultValue));
            }

            @Override
            public void forEach(BiConsumer<? super Integer, ? super IntHashSet> action) {
                super.forEach((key, values) -> action.accept(key, copy(values)));
            }

            private static @Nullable IntHashSet copy(@Nullable IntHashSet container) {
                return container != null ? new IntHashSet(container) : null;
            }
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
