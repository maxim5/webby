package io.webby.demo.model;

import com.carrotsearch.hppc.LongArrayList;
import io.webby.orm.api.Connector;
import io.webby.orm.api.entity.BatchEntityLongData;
import io.webby.orm.api.entity.EntityData;
import io.webby.orm.api.entity.EntityLongData;
import io.webby.orm.api.query.Contextual;
import io.webby.orm.api.query.TermType;
import io.webby.orm.api.query.Where;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.BaseTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
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

    /** {@link LongsModelTable#insert(LongsModel)} **/

    @Test
    public void insert_ok() {
        assertEquals(table.insert(new LongsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void insert_duplicate_ok() {
        assertEquals(table.insert(new LongsModel(1, 2, 3)), 1);
        assertEquals(table.insert(new LongsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3));
    }

    /** {@link LongsModelTable#insertIgnore(LongsModel)} **/

    @Test
    public void insert_ignore_ok() {
        assertEquals(table.insertIgnore(new LongsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void insert_ignore_duplicate_ok() {
        assertEquals(table.insertIgnore(new LongsModel(1, 2, 3)), 1);
        assertEquals(table.insertIgnore(new LongsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3));
    }

    /** {@link LongsModelTable#insertData(EntityData)} **/

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

    /** {@link LongsModelTable#insertBatch(Collection)} **/

    @Test
    public void insert_batch_of_one_ok() {
        List<LongsModel> batch = List.of(new LongsModel(1, 2, 3));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void insert_batch_of_two_different_ok() {
        List<LongsModel> batch = List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6));
    }

    @Test
    public void insert_batch_of_two_duplicates_ok() {
        List<LongsModel> batch = List.of(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3));
    }

    /** {@link LongsModelTable#insertDataBatch(BatchEntityData)} **/

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

    /** {@link LongsModelTable#updateWhere(LongsModel, Where)} **/

    @Test
    public void update_where_ok() {
        table.insert(new LongsModel(1, 2, 3));

        int updated = table.updateWhere(
            new LongsModel(1, 2, 333),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333));
    }

    @Test
    public void update_where_does_not_match() {
        table.insert(new LongsModel(1, 2, 3));

        int updated = table.updateWhere(
            new LongsModel(1, 2, 333),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void update_where_multiple_rows_match() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3)));

        int updated = table.updateWhere(
            new LongsModel(1, 2, 333),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 2);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(1, 2, 333));
    }

    /** {@link LongsModelTable#updateDataWhere(EntityData, Where)} **/

    @Test
    public void update_data_where_complete_ok() {
        table.insert(new LongsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333));
    }

    @Test
    public void update_data_where_incomplete_one_column_ok() {
        table.insert(new LongsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            new EntityLongData(List.of(bar), LongArrayList.from(777)),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 777, 3));
    }

    @Test
    public void update_data_where_does_not_match() {
        table.insert(new LongsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3));
    }

    @Test
    public void update_data_where_multiple_rows_match() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 2);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(1, 2, 333));
    }

    /** {@link LongsModelTable#updateWhereBatch(Collection, Contextual)} **/

    @Test
    public void update_where_batch_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6)));

        int[] updated = table.updateWhereBatch(
            List.of(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.foo()))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666));
    }

    @Test
    public void update_where_batch_extra_values_ok() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(4, 5, 6)));

        int[] updated = table.updateWhereBatch(
            List.of(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666), new LongsModel(7, 8, 9)),
            Contextual.resolvingByName(
                Where.and(lookupBy(foo, unresolved("x", TermType.NUMBER)), lookupBy(bar, unresolved("y", TermType.NUMBER))),
                row -> Map.of("x", row.foo(), "y", row.bar()))
        );
        assertThat(updated).asList().containsExactly(1, 1, 0);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(4, 5, 666));
    }

    @Test
    public void update_where_batch_multiple_rows_match() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3)));

        int[] updated = table.updateWhereBatch(
            List.of(new LongsModel(1, 2, 333)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.foo()))
        );
        assertThat(updated).asList().containsExactly(2);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(1, 2, 333));
    }

    /** {@link LongsModelTable#updateDataWhereBatch(BatchEntityData, Contextual)} **/

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

    @Test
    public void update_data_where_batch_multiple_rows_match() {
        table.insertBatch(List.of(new LongsModel(1, 2, 3), new LongsModel(1, 2, 3)));

        int[] updated = table.updateDataWhereBatch(
            newLongsModelBatch(LongArrayList.from(1, 2, 333)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.get(0)))
        );
        assertThat(updated).asList().containsExactly(2);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333), new LongsModel(1, 2, 333));
    }

    /** {@link IntsModelTable#updateWhereOrInsert(Object, Where)} **/

    @Test
    public void update_where_or_insert_updated() {
        table.insert(new LongsModel(1, 2, 3));

        int affected = table.updateWhereOrInsert(
            new LongsModel(1, 2, 333),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333));
    }

    @Test
    public void update_where_or_insert_inserted() {
        table.insert(new LongsModel(1, 2, 3));

        int affected = table.updateWhereOrInsert(
            new LongsModel(111, 222, 333),
            Where.and(lookupBy(foo, var(111)), lookupBy(bar, var(222)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(111, 222, 333));
    }

    /** {@link LongsModelTable#updateWhereOrInsertData(EntityData, Where)} **/

    @Test
    public void update_where_or_insert_data_complete_updated() {
        table.insert(new LongsModel(1, 2, 3));

        int affected = table.updateWhereOrInsertData(
            newLongsModelData(LongArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 333));
    }

    @Test
    public void update_where_or_insert_data_complete_inserted() {
        table.insert(new LongsModel(1, 2, 3));

        int affected = table.updateWhereOrInsertData(
            newLongsModelData(LongArrayList.from(111, 222, 333)),
            Where.and(lookupBy(foo, var(111)), lookupBy(bar, var(222)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new LongsModel(1, 2, 3), new LongsModel(111, 222, 333));
    }
}
