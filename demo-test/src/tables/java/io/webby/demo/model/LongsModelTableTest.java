package io.webby.demo.model;

import com.carrotsearch.hppc.LongArrayList;
import io.webby.orm.api.Connector;
import io.webby.orm.api.entity.BatchEntityLongData;
import io.webby.orm.api.entity.EntityLongData;
import io.webby.orm.api.query.Contextual;
import io.webby.orm.api.query.TermType;
import io.webby.orm.api.query.Where;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.BaseTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.demo.model.LongsModelTable.OwnColumn.*;
import static io.webby.demo.model.LongsModelTable.newLongsModelBatch;
import static io.webby.demo.model.LongsModelTable.newLongsModelData;
import static io.webby.orm.api.query.Shortcuts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LongsModelTableTest
        extends SqlDbTableTest<LongsModel, LongsModelTable>
        implements BaseTableTest<LongsModel, LongsModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new LongsModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Test
    public void invalid_params_throws() {
        assertThrows(AssertionError.class, () -> newLongsModelData(LongArrayList.from(1, 2)));
        assertThrows(AssertionError.class, () -> newLongsModelBatch(LongArrayList.from(1, 2, 3, 4)));
    }

    @Test
    public void insert_data_complete_ok() {
        EntityLongData data = newLongsModelData(LongArrayList.from(1, 2, 3));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void insert_data_incomplete_two_columns_ok() {
        EntityLongData data = new EntityLongData(List.of(foo, bar), LongArrayList.from(1, 2));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 0));
    }

    @Test
    public void insert_data_incomplete_one_column_ok() {
        EntityLongData data = new EntityLongData(List.of(bar), LongArrayList.from(777));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(0, 777, 0));
    }

    @Test
    public void insert_data_batch_complete_ok() {
        BatchEntityLongData batchData = newLongsModelBatch(LongArrayList.from(1, 2, 3, 4, 5, 6));
        assertThat(table.insertDataBatch(batchData)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6));
    }

    @Test
    public void insert_data_batch_incomplete_two_columns_ok() {
        BatchEntityLongData batchData = new BatchEntityLongData(List.of(foo, bar), LongArrayList.from(1, 2, 4, 5));
        assertThat(table.insertDataBatch(batchData)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 0), new LongsModel(4, 5, 0));
    }

    @Test
    public void update_data_where_complete_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333));
    }

    @Test
    public void update_data_where_incomplete_one_column_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            new EntityLongData(List.of(bar), LongArrayList.from(777)),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 777, 3));
    }

    @Test
    public void update_data_where_not_found() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void update_data_where_batch_complete_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            newLongsModelBatch(LongArrayList.from(1, 2, 333, 4, 5, 666)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.get(0)))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666));
    }

    @Test
    public void update_data_where_batch_complete_extra_values_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            newLongsModelBatch(LongArrayList.from(1, 2, 333, 4, 5, 666, 7, 8, 9)),
            Contextual.resolvingByName(
                Where.and(lookupBy(foo, unresolved("x", TermType.NUMBER)), lookupBy(bar, unresolved("y", TermType.NUMBER))),
                row -> Map.of("x", row.get(0), "y", row.get(1)))
        );
        assertThat(updated).asList().containsExactly(1, 1, 0);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666));
    }

    @Test
    public void update_data_where_batch_incomplete_columns_wrong_order_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            new BatchEntityLongData(List.of(value, bar), LongArrayList.from(333, 2, 666, 5)),
            Contextual.resolvingByOrderedList(
                Where.of(lookupBy(bar, unresolved("x", TermType.NUMBER))),
                row -> List.of(row.get(1)))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666));
    }
}
