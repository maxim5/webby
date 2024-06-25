package io.spbx.webby.db.count.vote;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.google.common.flogger.FluentLogger;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.QueryException;
import io.spbx.orm.api.entity.BatchEntityIntData;
import io.spbx.orm.api.entity.EntityIntData;
import io.spbx.orm.api.query.*;
import io.spbx.util.hppc.EasyHppc;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.db.StorageType;
import io.spbx.webby.db.count.StoreId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import static io.spbx.orm.api.query.Shortcuts.*;
import static io.spbx.webby.app.Settings.SQL_MAX_PARAMS;
import static io.spbx.webby.db.count.vote.Consistency.checkStorageConsistency;

public class TableVotingStorage implements VotingStorage {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final StoreId storeId;
    private final BaseTable<?> table;
    private final Column keyColumn;
    private final Column actorColumn;
    private final Column valueColumn;

    public TableVotingStorage(@NotNull BaseTable<?> table,
                              @NotNull Column keyColumn,
                              @NotNull Column actorColumn,
                              @NotNull Column valueColumn) {
        this.storeId = new StoreId(StorageType.SQL_DB, table.meta().sqlTableName());
        this.table = table;
        this.keyColumn = keyColumn;
        this.actorColumn = actorColumn;
        this.valueColumn = valueColumn;
    }

    @Override
    public @NotNull StoreId storeId() {
        return storeId;
    }

    @Override
    public @NotNull IntHashSet load(int key) {
        SelectWhere query = SelectWhere.from(table).select(actorColumn, valueColumn)
            .where(Where.of(lookupBy(keyColumn, key)))
            .build();
        IntHashSet set = new IntHashSet();
        table.runner().forEach(query, resultSet -> {
            int actor = resultSet.getInt(1);
            int value = resultSet.getInt(2);
            assert value >= -1 && value <= 1 : "Value outside of range: " + value;
            set.add(value > 0 ? actor : -actor);
        });
        return set;
    }

    @Override
    public void loadBatch(@NotNull IntContainer keys, @NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        if (!keys.isEmpty()) {
            EasyHppc.iterateChunks(keys, AppSettings.live().getInt(SQL_MAX_PARAMS), chunk ->
                loadQueryResults(builder -> builder.where(Where.of(isIn(keyColumn, makeIntVariables(chunk)))), consumer)
            );
        }
    }

    @Override
    public void loadAll(@NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        loadQueryResults(builder -> {}, consumer);
    }

    private void loadQueryResults(@NotNull Consumer<SelectWhere.Builder> updater,
                                  @NotNull IntObjectProcedure<@NotNull IntHashSet> consumer) {
        SelectWhere query = SelectWhere.from(table).select(keyColumn, actorColumn, valueColumn).applying(updater).build();

        IntObjectHashMap<IntHashSet> map = new IntObjectHashMap<>();
        table.runner().forEach(query, resultSet -> {
            int key = resultSet.getInt(1);
            int actor = resultSet.getInt(2);
            int value = resultSet.getInt(3);
            assert value >= -1 && value <= 1 : "Value outside of range: " + value;
            IntHashSet set = map.get(key);
            if (set == null) {
                set = new IntHashSet();
                map.put(key, set);
            }
            set.add(value >= 0 ? actor : -actor);
        });
        map.forEach(consumer);
    }

    @Override
    public void storeBatch(@NotNull IntObjectMap<IntHashSet> curr, @Nullable IntObjectMap<IntHashSet> prev) {
        assert prev == null || checkStorageConsistency(this, curr, prev) : "This is Impossible?!";
        if (prev == null) {
            prev = loadBatch(curr.keys());
        }

        Diff diff = new Diff(curr, prev);
        List<Column> columns = List.of(keyColumn, actorColumn, valueColumn);

        try {
            // Insert
            if (!diff.added.isEmpty()) {
                table.insertDataBatch(new BatchEntityIntData(columns, diff.added));
            }

            // Update
            if (!diff.modified.isEmpty()) {
                table.updateDataWhereBatch(
                    new BatchEntityIntData(columns, diff.modified),
                    Contextual.resolvingByOrderedList(
                        Where.and(lookupBy(keyColumn, UNRESOLVED_NUM), lookupBy(actorColumn, UNRESOLVED_NUM)),
                        array -> List.of(array.get(0), array.get(1))
                    )
                );
            }

            // Delete
            for (IntObjectCursor<IntHashSet> cursor : diff.deleted) {
                table.deleteWhere(Where.and(
                    lookupBy(keyColumn, cursor.key),
                    isIn(actorColumn, makeIntVariables(cursor.value))
                ));
            }
        } catch (QueryException e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to store batch. Fall back to ultra-safe method");
            storeBatchUltraSafe(curr);
        }
    }

    private void storeBatchUltraSafe(@NotNull IntObjectMap<IntHashSet> map) {
        for (IntObjectCursor<IntHashSet> entry : map) {
            for (IntCursor cursor : entry.value) {
                int key = entry.key;
                int actor = Math.abs(cursor.value);
                int value = cursor.value >= 0 ? 1 : -1;
                table.updateWhereOrInsertData(
                    new EntityIntData(List.of(valueColumn), IntArrayList.from(value)),
                    Where.and(lookupBy(keyColumn, key), lookupBy(actorColumn, actor))
                );
            }
        }
    }

    private static class Diff {
        private final IntArrayList added = new IntArrayList();
        private final IntArrayList modified = new IntArrayList();
        private final IntObjectMap<IntHashSet> deleted = new IntObjectHashMap<>();

        public Diff(@NotNull IntObjectMap<IntHashSet> curr, @NotNull IntObjectMap<IntHashSet> prev) {
            IntHashSet allKeys = EasyHppc.union(curr.keys(), prev.keys());
            for (IntCursor cursorK : allKeys) {
                int key = cursorK.value;
                IntHashSet valuesNow = curr.get(key);
                IntHashSet valuesBefore = prev.get(key);

                if (valuesNow == null) {
                    addPositivesTo(key, valuesBefore, deleted);
                    continue;
                }
                if (valuesBefore == null) {
                    addTriplesTo(key, valuesNow, added);
                    continue;
                }

                for (IntCursor cursorV : valuesNow) {
                    int value = cursorV.value;
                    if (valuesBefore.contains(-value)) {
                        addTripleTo(key, value, modified);
                    } else if (!valuesBefore.contains(value)) {
                        addTripleTo(key, value, added);
                    }
                }

                addPositivesTo(
                    key,
                    EasyHppc.removeAllCopy(valuesBefore, value -> valuesNow.contains(value) || valuesNow.contains(-value)),
                    deleted
                );
            }
        }

        private static void addTriplesTo(int key, @NotNull IntContainer values, @NotNull IntArrayList dest) {
            dest.ensureCapacity(dest.size() + values.size() * 3);
            for (IntCursor cursor : values) {
                addTripleTo(key, cursor.value, dest);
            }
        }

        private static void addTripleTo(int key, int value, @NotNull IntArrayList dest) {
            dest.add(key);
            if (value >= 0) {
                dest.add(value, 1);
            } else {
                dest.add(-value, -1);
            }
        }

        private static void addPositivesTo(int key, @NotNull IntContainer values, @NotNull IntObjectMap<IntHashSet> dest) {
            if (!values.isEmpty()) {
                IntHashSet set = dest.get(key);
                if (set == null) {
                    set = new IntHashSet();
                    dest.put(key, set);
                }
                for (IntCursor cursor : values) {
                    set.add(Math.abs(cursor.value));
                }
            }
        }
    }

    private static @NotNull List<Variable> makeIntVariables(@NotNull IntContainer container) {
        return EasyHppc.toJavaStream(container).map(cursor -> var(cursor.value)).toList();
    }
}
