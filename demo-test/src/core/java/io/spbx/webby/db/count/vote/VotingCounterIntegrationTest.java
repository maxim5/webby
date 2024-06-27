package io.spbx.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.google.common.collect.HashBasedTable;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.mu.util.stream.BiStream;
import io.spbx.util.hppc.EasyHppc;
import io.spbx.webby.db.DbReadyEvent;
import io.spbx.webby.db.StorageType;
import io.spbx.webby.db.count.StoreChangedEvent;
import io.spbx.webby.db.kv.javamap.JavaMapDbFactory;
import io.spbx.webby.demo.model.UserRateModelTable;
import io.spbx.webby.testing.ext.SqlDbExtension;
import io.spbx.webby.testing.ext.TestingGuiceExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.AssertHppc.assertMap;
import static io.spbx.util.testing.TestingHppc.newIntMap;
import static io.spbx.util.testing.TestingHppc.newIntObjectMap;
import static io.spbx.webby.db.count.vote.Vote.none;
import static io.spbx.webby.db.count.vote.Vote.votes;
import static io.spbx.webby.demo.model.UserRateModelTable.OwnColumn.*;
import static java.util.Objects.requireNonNull;

@Tag("sql")
public class VotingCounterIntegrationTest {
    @RegisterExtension private static final TestingGuiceExtension GUICE = TestingGuiceExtension.lite();
    @RegisterExtension private static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserRateModelTable.META);

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

        assertThat(counter.estimateCount(A)).isEqualTo(0);
        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void empty_state_flush(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_simple(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);

        assertCounter(counter).hasCountEstimates(A, 1, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(+A, none(B), none(C)),
                                              Bob, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void increment_double(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(A, Ann)).isEqualTo(1);

        assertCounter(counter).hasCountEstimates(A, 1, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(+A, none(B), none(C)),
                                              Bob, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_simple(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);

        assertCounter(counter).hasCountEstimates(A, -1, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(-A, none(B), none(C)),
                                              Bob, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void decrement_double(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);

        assertCounter(counter).hasCountEstimates(A, -1, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(-A, none(B), none(C)),
                                              Bob, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(A, Ann)).isEqualTo(0);

        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A), none(B), none(C)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_multi_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        assertThat(counter.increment(A, Ann)).isEqualTo(0);
        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(A, Ann)).isEqualTo(0);
        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);

        assertCounter(counter).hasCountEstimates(A, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_different_keys(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(B, Ann)).isEqualTo(1);
        assertThat(counter.increment(C, Ann)).isEqualTo(1);

        assertCounter(counter).hasCountEstimates(A, 1, B, 1, C, 1);
        assertCounter(counter).hasActorValues(Ann, votes(+A, +B, +C));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(A, Bob)).isEqualTo(2);
        assertThat(counter.increment(A, Liz)).isEqualTo(3);

        assertCounter(counter).hasCountEstimates(A, 3, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(+A), Bob, votes(+A), Liz, votes(+A));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_inc_dec_same_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(A, Bob)).isEqualTo(2);
        assertThat(counter.increment(A, Liz)).isEqualTo(3);
        assertThat(counter.decrement(A, Ann)).isEqualTo(2);
        assertThat(counter.decrement(A, Bob)).isEqualTo(1);
        assertThat(counter.decrement(A, Liz)).isEqualTo(0);

        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A)), Bob, votes(none(A)), Liz, votes(none(A)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_users_inc_dec_different_key(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(A, Bob)).isEqualTo(0);
        assertThat(counter.increment(B, Liz)).isEqualTo(1);
        assertThat(counter.decrement(B, Ann)).isEqualTo(0);
        assertThat(counter.increment(C, Bob)).isEqualTo(1);
        assertThat(counter.increment(C, Liz)).isEqualTo(2);

        assertCounter(counter).hasCountEstimates(A, 0, B, 0, C, 2);
        assertCounter(counter).hasActorValues(Ann, votes(+A, -B, none(C)),
                                              Bob, votes(-A, +C, none(B)),
                                              Liz, votes(+B, +C, none(A)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_inc_flush(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(B, Bob)).isEqualTo(1);
        assertThat(counter.increment(A, Liz)).isEqualTo(2);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 2, B, 1);
        assertCounter(counter).hasActorValues(Ann, votes(+A), Bob, votes(+B), Liz, votes(+A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann, +Liz),
                                                         B, IntHashSet.from(+Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void many_users_multi_dec_flush(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        assertThat(counter.decrement(B, Bob)).isEqualTo(-1);
        assertThat(counter.decrement(A, Liz)).isEqualTo(-2);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -2, B, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A), Bob, votes(-B), Liz, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann, -Liz),
                                                         B, IntHashSet.from(-Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_double_flush(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.increment(B, Ann)).isEqualTo(1);
        counter.flush();
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 1, B, 1);
        assertCounter(counter).hasActorValues(Ann, votes(+A, +B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann),
                                                         B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_inc_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        counter.flush();
        assertThat(counter.increment(B, Ann)).isEqualTo(1);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 1, B, 1);
        assertCounter(counter).hasActorValues(Ann, votes(+A, +B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann),
                                                         B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_inc_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        counter.flush();
        assertThat(counter.decrement(A, Ann)).isEqualTo(0);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_undo_dec_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        counter.flush();
        assertThat(counter.increment(A, Ann)).isEqualTo(0);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_flip_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        counter.flush();
        assertThat(counter.decrement(A, Ann)).isEqualTo(0);
        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void one_user_no_change_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(B, Ann)).isEqualTo(-1);
        counter.flush();
        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(B, Ann)).isEqualTo(-1);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 1, B, -1);
        assertCounter(counter).hasActorValues(Ann, votes(+A, -B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann),
                                                         B, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void multi_changes_between_flushes(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        assertThat(counter.decrement(B, Ann)).isEqualTo(-1);
        counter.flush();
        assertThat(counter.decrement(C, Ann)).isEqualTo(-1);
        assertThat(counter.increment(B, Bob)).isEqualTo(0);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 1, B, 0, C, -1);
        assertCounter(counter).hasActorValues(Ann, votes(+A, -B, -C),
                                              Bob, votes(+B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann),
                                                         B, IntHashSet.from(-Ann, +Bob),
                                                         C, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_load_counts(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(Ann),
                                        B, IntHashSet.from(-Bob)));

        assertCounter(counter).hasCountEstimates(A, 1, B, -1, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(+A, none(B), none(C)),
                                              Bob, votes(none(A), -B, none(C)));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann),
                                                         B, IntHashSet.from(-Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_no_changes_flush(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_undo_inc(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(Ann)));

        assertThat(counter.decrement(A, Ann)).isEqualTo(0);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 0);
        assertCounter(counter).hasActorValues(Ann, votes(none(A)));
        assertStorage(storage).isEqualTo(StorageState.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_flip(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertThat(counter.increment(A, Ann)).isEqualTo(0);
        assertThat(counter.increment(A, Ann)).isEqualTo(1);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 1);
        assertCounter(counter).hasActorValues(Ann, votes(+A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_new_key_changed(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertThat(counter.increment(B, Ann)).isEqualTo(1);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -1, B, 1);
        assertCounter(counter).hasActorValues(Ann, votes(-A, +B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann),
                                                         B, IntHashSet.from(+Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_new_user_same_key(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann)));

        assertThat(counter.decrement(A, Bob)).isEqualTo(-2);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -2);
        assertCounter(counter).hasActorValues(Ann, votes(-A),
                                              Bob, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann, -Bob)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_multi_changes_simple(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann),
                                        B, IntHashSet.from(-Bob)));

        assertThat(counter.increment(A, Bob)).isEqualTo(2);
        assertThat(counter.increment(A, Liz)).isEqualTo(3);
        assertThat(counter.decrement(B, Ann)).isEqualTo(-2);
        assertThat(counter.decrement(B, Liz)).isEqualTo(-3);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 3, B, -3, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(+A, -B),
                                              Bob, votes(+A, -B),
                                              Liz, votes(+A, -B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann, +Bob, +Liz),
                                                         B, IntHashSet.from(-Ann, -Bob, -Liz)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void existing_state_multi_changes_various(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(-Ann, +Bob),
                                        B, IntHashSet.from(+Ann, +Bob, +Liz)));

        assertThat(counter.decrement(A, Ann)).isEqualTo(0);     // double
        assertThat(counter.decrement(A, Liz)).isEqualTo(-1);    // new
        assertThat(counter.decrement(A, Bob)).isEqualTo(-2);    // undo
        assertThat(counter.decrement(A, Bob)).isEqualTo(-3);    // flip
        assertThat(counter.decrement(B, Ann)).isEqualTo(2);     // undo
        assertThat(counter.increment(B, Bob)).isEqualTo(2);     // double
        assertThat(counter.increment(C, Bob)).isEqualTo(1);     // new
        assertThat(counter.decrement(C, Liz)).isEqualTo(0);     // new
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -3, B, 2, C, 0);
        assertCounter(counter).hasActorValues(Ann, votes(-A, none(B), none(C)),
                                              Bob, votes(-A, +B, +C),
                                              Liz, votes(-A, +B, -C));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann, -Bob, -Liz),
                                                         B, IntHashSet.from(+Bob, +Liz),
                                                         C, IntHashSet.from(+Bob, -Liz)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_db_row_deleted(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertThat(counter.decrement(A, Ann)).isEqualTo(0);
        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);  // flipped

        pushToStorage(StorageState.EMPTY);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_db_row_inserted(Scenario scenario) {
        setup(scenario, StorageState.EMPTY);

        assertThat(counter.decrement(A, Ann)).isEqualTo(-1);

        pushToStorage(StorageState.of(A, IntHashSet.from(+Ann)));
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, -1);
        assertCounter(counter).hasActorValues(Ann, votes(-A));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_unrelated_db_row_deleted(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertThat(counter.decrement(B, Ann)).isEqualTo(-1);

        pushToStorage(StorageState.EMPTY);
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 0, B, -1);
        assertCounter(counter).hasActorValues(Ann, votes(none(A), -B));
        assertStorage(storage).isEqualTo(StorageState.of(B, IntHashSet.from(-Ann)));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void external_change_unrelated_db_row_inserted(Scenario scenario) {
        setup(scenario, StorageState.of(A, IntHashSet.from(+Ann)));

        assertThat(counter.increment(A, Bob)).isEqualTo(2);

        pushToStorage(StorageState.of(B, IntHashSet.from(-Bob)));
        counter.flush();

        assertCounter(counter).hasCountEstimates(A, 2, B, -1);
        assertCounter(counter).hasActorValues(Ann, votes(+A),
                                              Bob, votes(+A, -B));
        assertStorage(storage).isEqualTo(StorageState.of(A, IntHashSet.from(+Ann, +Bob),
                                                         B, IntHashSet.from(-Bob)));
    }

    @Tag("slow")
    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void stress_test(Scenario scenario) {
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
                    assertThat(counter.increment(key, user)).isEqualTo(sum);
                } else {
                    assertThat(counter.decrement(key, user)).isEqualTo(sum);
                }
            }

            // assert votes
            for (Integer voter : expectedVotes.columnKeySet()) {
                Map<Integer, Integer> expectedColumn = expectedVotes.column(voter);
                IntIntMap actualVotes = counter.getVotes(EasyHppc.fromJavaIterableInt(expectedColumn.keySet()), voter);
                assertMap(actualVotes).asJavaMap().isEqualTo(expectedColumn);
            }

            // assert counts
            IntIntMap counts = counter.estimateCounts(EasyHppc.fromJavaIterableInt(expectedVotes.rowKeySet()));
            for (IntIntCursor cursor : counts) {
                int sum = expectedVotes.row(cursor.key).values().stream().mapToInt(x -> x).sum();
                assertThat(cursor.value).isEqualTo(sum);
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

    @CheckReturnValue
    private static @NotNull VotingCounterSubject assertCounter(@NotNull VotingCounter counter) {
        return new VotingCounterSubject(counter);
    }

    @CheckReturnValue
    private static @NotNull VotingStorageSubject assertStorage(@NotNull VotingStorage storage) {
        return new VotingStorageSubject(storage);
    }

    private record VotingCounterSubject(@NotNull VotingCounter counter) {
        public void hasCountEstimates(int... expected) {
            IntIntHashMap expectedMap = newIntMap(expected);

            assertMap(counter.estimateCounts(expectedMap.keys())).trimmed().containsExactlyTrimmed(expected);
            for (IntIntCursor cursor : expectedMap) {
                assertThat(counter.estimateCount(cursor.key)).isEqualTo(cursor.value);
            }
        }

        public void hasActorValues(@NotNull Object @NotNull ... expected) {
            IntObjectMap<List<Vote>> expectedMap = newIntObjectMap(expected);

            for (IntObjectCursor<List<Vote>> cursor : expectedMap) {
                int user = cursor.key;
                for (Vote vote : cursor.value) {
                    // With custom error message: "user=%d expected=%s".formatted(user, vote)
                    assertThat(counter.getVote(vote.key(), user)).isEqualTo(vote.val());
                    assertMap(counter.getVotes(IntArrayList.from(vote.key()), user)).containsExactly(vote.key(), vote.val());
                }

                IntArrayList keys = EasyHppc.fromJavaIterableInt(cursor.value.stream().map(Vote::key).toList());
                Map<Integer, Integer> expectedActorValues =
                    BiStream.biStream(cursor.value.stream()).mapKeys(Vote::key).mapValues(Vote::val).toMap();
                assertMap(counter.getVotes(keys, user)).asJavaMap().isEqualTo(expectedActorValues);
            }
        }
    }

    private record VotingStorageSubject(@NotNull VotingStorage storage) {
        public void isEqualTo(@NotNull StorageState state) {
            assertThat(storage.loadAll()).isEqualTo(state.map());
        }
    }

    public void pushToStorage(@NotNull StorageState state) {
        IntObjectMap<IntHashSet> map = new IntObjectHashMap<>(state.map());
        for (IntObjectCursor<IntHashSet> cursor : storage.loadAll()) {
            map.putIfAbsent(cursor.key, new IntHashSet());
        }

        storage.storeBatch(map, null);  // FIX[minor]: add a dedicated method for testing?
        assertStorage(storage).isEqualTo(state);

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

        eventBus.post(new DbReadyEvent());
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
