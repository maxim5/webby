package io.spbx.webby.benchmarks.jmh;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.eventbus.EventBus;
import io.spbx.orm.api.query.Shortcuts;
import io.spbx.orm.api.query.Where;
import io.spbx.webby.db.StorageType;
import io.spbx.webby.db.count.vote.*;
import io.spbx.webby.db.sql.SqlSettings;
import io.webby.demo.model.UserRateModelTable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.webby.demo.model.UserRateModelTable.OwnColumn.*;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
public class VotingCounterJmhBenchmark {
    @SuppressWarnings("FieldMayBeFinal")
    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({"1000"}) private int users = 0;
        @Param({"10000"}) private int keys = 0;

        @Param({"10"}) private int batchLookupIterations = 0;
        @Param({"50"}) private int keysBatchSize = 0;

        private final StorageType storeType = StorageType.SQL_DB;
        private final VotingCounterType counterType = VotingCounterType.NON_BLOCKING;

        private Connection connection;
        private VotingStorage storage;
        private VotingCounter counter;
        private final Random random = new Random(0);

        @Setup(Level.Trial)
        public void setUp(BenchmarkParams params) {
            connection = SqlSettings.connectNotForProduction(SqlSettings.MYSQL_TEST);
            UserRateModelTable table = new UserRateModelTable(() -> connection);

            /*
            CREATE TABLE IF NOT EXISTS `user_rate_model` (
                user_id MEDIUMINT UNSIGNED NOT NULL,
                content_id MEDIUMINT UNSIGNED NOT NULL,
                value SMALLINT NOT NULL,
                PRIMARY KEY (content_id, user_id)
            ) ENGINE=InnoDB;
             */
            table.deleteWhere(Where.of(Shortcuts.TRUE));

            storage = switch (storeType) {
                case SQL_DB -> new TableVotingStorage(table, content_id, user_id, value);
                case KEY_VALUE_DB -> throw new UnsupportedOperationException();
            };

            counter = switch (counterType) {
                case LOCK_BASED -> new LockBasedVotingCounter(storage, new EventBus());
                case NON_BLOCKING -> new NonBlockingVotingCounter(storage, new EventBus());
            };

            int keys = Integer.parseInt(params.getParam("keys"));
            int users = Integer.parseInt(params.getParam("users"));
            int keysBatchSize = Integer.parseInt(params.getParam("keysBatchSize"));

            IntArrayList allKeys = IntArrayList.from(IntStream.range(1, keys + 1).toArray());

            IntObjectHashMap<IntHashSet> map = new IntObjectHashMap<>();
            for (IntCursor cursor : allKeys) {
                IntHashSet set = IntHashSet.from(IntStream.range(1, users + 1).map(i -> i % 2 == 0 ? i : -i).toArray());
                map.put(cursor.value, set);
            }

            // long start = System.currentTimeMillis();
            storage.storeBatch(map, null);
            // long insertDuration = System.currentTimeMillis() - start;
            // System.out.println("insert: " + insertDuration + " ms");
        }

        @TearDown(Level.Trial)
        public void tearDown() throws SQLException {
            connection.close();
        }
    }

    @Benchmark
    public void estimate_counts_batch(ExecutionPlan plan) {
        for (int i = 0; i < plan.batchLookupIterations; i++) {
            // int from = plan.random.nextInt(plan.keys - plan.keysBatchSize);
            // IntArrayList keys = EasyHppc.slice(plan.allKeys, from, from + plan.keysBatchSize);
            IntArrayList keys = IntArrayList.from(IntStream.range(0, plan.keysBatchSize).map(j -> plan.random.nextInt(plan.keys)).toArray());
            IntIntMap counts = plan.counter.estimateCounts(keys);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("jmh.separateClasspathJAR", "true");
        Options options = new OptionsBuilder().include(VotingCounterJmhBenchmark.class.getSimpleName()).build();
        new Runner(options).run();
    }
}
