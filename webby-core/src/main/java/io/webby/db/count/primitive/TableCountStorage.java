package io.webby.db.count.primitive;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.procedures.IntIntProcedure;
import com.google.common.flogger.FluentLogger;
import io.spbx.orm.api.BaseTable;
import io.spbx.orm.api.entity.EntityIntData;
import io.spbx.orm.api.query.*;
import io.spbx.util.base.OneOf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.spbx.orm.api.query.Shortcuts.lookupBy;

public class TableCountStorage implements IntCountStorage {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final BaseTable<?> table;
    private final Column keyColumn;
    private final Column counterColumn;
    private final Column itemColumn;

    public TableCountStorage(@NotNull BaseTable<?> table, @NotNull Column keyColumn, @NotNull OneOf<Column, Column> valueColumn) {
        this.table = table;
        this.keyColumn = keyColumn;
        this.counterColumn = valueColumn.first();
        this.itemColumn = valueColumn.second();
    }

    @Override
    public void loadAll(@NotNull IntIntProcedure consumer) {
        SelectQuery query;
        if (counterColumn != null) {
            query = SelectWhere.from(table).select(keyColumn, counterColumn).build();
        } else {
            query = SelectGroupBy.from(table).groupBy(keyColumn).aggregate(Func.COUNT.apply(itemColumn)).build();
        }

        table.runner().forEach(query, resultSet -> {
            int key = resultSet.getInt(1);
            int count = resultSet.getInt(2);
            consumer.apply(key, count);
        });
    }

    @Override
    public void storeBatch(@NotNull IntIntMap map) {
        if (counterColumn != null) {
            for (IntIntCursor cursor : map) {
                table.updateWhereOrInsertData(
                    new EntityIntData(List.of(counterColumn), IntArrayList.from(cursor.value)),
                    Where.of(lookupBy(keyColumn, cursor.key))
                );
            }
        } else {
            log.atInfo().log("Storage %s is self-managed: not storing the count map from cache", this);
        }
    }
}
