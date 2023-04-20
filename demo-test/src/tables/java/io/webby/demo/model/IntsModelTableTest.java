package io.webby.demo.model;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.api.entity.BatchEntityData;
import io.webby.orm.api.entity.BatchEntityIntData;
import io.webby.orm.api.entity.EntityData;
import io.webby.orm.api.entity.EntityIntData;
import io.webby.orm.api.query.Contextual;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.TermType;
import io.webby.orm.api.query.Where;
import io.webby.testing.BaseTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.demo.model.IntsModelTable.OwnColumn.*;
import static io.webby.demo.model.IntsModelTable.newIntsModelBatch;
import static io.webby.demo.model.IntsModelTable.newIntsModelData;
import static io.webby.orm.api.query.Shortcuts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntsModelTableTest
        extends SqlDbTableTest<IntsModel, IntsModelTable>
        implements BaseTableTest<IntsModel, IntsModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new IntsModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Test
    public void invalid_params_throws() {
        assertThrows(AssertionError.class, () -> newIntsModelData(IntArrayList.from(1, 2)));
        assertThrows(AssertionError.class, () -> newIntsModelBatch(IntArrayList.from(1, 2, 3, 4)));
    }

    /** {@link IntsModelTable#insert(IntsModel)} **/

    @Test
    public void insert_ok() {
        assertEquals(table.insert(new IntsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void insert_duplicate_ok() {
        assertEquals(table.insert(new IntsModel(1, 2, 3)), 1);
        assertEquals(table.insert(new IntsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3));
    }

    /** {@link IntsModelTable#insertIgnore(IntsModel)} **/

    @Test
    public void insert_ignore_ok() {
        assumeOneOfEngines(Engine.SQLite, Engine.MySQL);
        assertEquals(table.insertIgnore(new IntsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void insert_ignore_duplicate_ok() {
        assumeOneOfEngines(Engine.SQLite, Engine.MySQL);
        assertEquals(table.insertIgnore(new IntsModel(1, 2, 3)), 1);
        assertEquals(table.insertIgnore(new IntsModel(1, 2, 3)), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3));
    }

    /** {@link IntsModelTable#insertData(EntityData)} **/

    @Test
    public void insert_data_complete_ok() {
        EntityIntData data = newIntsModelData(IntArrayList.from(1, 2, 3));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void insert_data_incomplete_two_columns_ok() {
        EntityIntData data = new EntityIntData(List.of(foo, bar), IntArrayList.from(1, 2));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 0));
    }

    @Test
    public void insert_data_incomplete_one_column_ok() {
        EntityIntData data = new EntityIntData(List.of(bar), IntArrayList.from(777));
        assertEquals(table.insertData(data), 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(0, 777, 0));
    }

    /** {@link IntsModelTable#insertBatch(Collection)} **/

    @Test
    public void insert_batch_of_one_ok() {
        List<IntsModel> batch = List.of(new IntsModel(1, 2, 3));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void insert_batch_of_two_different_ok() {
        List<IntsModel> batch = List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6));
    }

    @Test
    public void insert_batch_of_two_duplicates_ok() {
        List<IntsModel> batch = List.of(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3));
        assertThat(table.insertBatch(batch)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3));
    }

    /** {@link IntsModelTable#insertDataBatch(BatchEntityData)} **/

    @Test
    public void insert_data_batch_complete_ok() {
        BatchEntityIntData batchData = newIntsModelBatch(IntArrayList.from(1, 2, 3, 4, 5, 6));
        assertThat(table.insertDataBatch(batchData)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6));
    }

    @Test
    public void insert_data_batch_incomplete_two_columns_ok() {
        BatchEntityIntData batchData = new BatchEntityIntData(List.of(foo, bar), IntArrayList.from(1, 2, 4, 5));
        assertThat(table.insertDataBatch(batchData)).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 0), new IntsModel(4, 5, 0));
    }

    /** {@link IntsModelTable#updateWhere(IntsModel, Where)} **/

    @Test
    public void update_where_ok() {
        table.insert(new IntsModel(1, 2, 3));

        int updated = table.updateWhere(
            new IntsModel(1, 2, 333),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333));
    }

    @Test
    public void update_where_does_not_match() {
        table.insert(new IntsModel(1, 2, 3));

        int updated = table.updateWhere(
            new IntsModel(1, 2, 333),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void update_where_multiple_rows_match() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3)));

        int updated = table.updateWhere(
            new IntsModel(1, 2, 333),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 2);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(1, 2, 333));
    }

    /** {@link IntsModelTable#updateDataWhere(EntityData, Where)} **/

    @Test
    public void update_data_where_complete_ok() {
        table.insert(new IntsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333));
    }

    @Test
    public void update_data_where_incomplete_one_column_ok() {
        table.insert(new IntsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            new EntityIntData(List.of(bar), IntArrayList.from(777)),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 777, 3));
    }

    @Test
    public void update_data_where_does_not_match() {
        table.insert(new IntsModel(1, 2, 3));

        int updated = table.updateDataWhere(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

    @Test
    public void update_data_where_multiple_rows_match() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 2);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(1, 2, 333));
    }

    /** {@link IntsModelTable#updateWhereBatch(Collection, Contextual)} **/

    @Test
    public void update_where_batch_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int[] updated = table.updateWhereBatch(
            List.of(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.foo()))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666));
    }

    @Test
    public void update_where_batch_extra_values_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int[] updated = table.updateWhereBatch(
            List.of(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666), new IntsModel(7, 8, 9)),
            Contextual.resolvingByName(
                Where.and(lookupBy(foo, unresolved("x", TermType.NUMBER)), lookupBy(bar, unresolved("y", TermType.NUMBER))),
                row -> Map.of("x", row.foo(), "y", row.bar()))
        );
        assertThat(updated).asList().containsExactly(1, 1, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666));
    }

    @Test
    public void update_where_batch_multiple_rows_match() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3)));

        int[] updated = table.updateWhereBatch(
            List.of(new IntsModel(1, 2, 333)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.foo()))
        );
        assertThat(updated).asList().containsExactly(2);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(1, 2, 333));
    }

    /** {@link IntsModelTable#updateDataWhereBatch(BatchEntityData, Contextual)} **/

    @Test
    public void update_data_where_batch_complete_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            newIntsModelBatch(IntArrayList.from(1, 2, 333, 4, 5, 666)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.get(0)))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666));
    }

    @Test
    public void update_data_where_batch_complete_extra_values_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            newIntsModelBatch(IntArrayList.from(1, 2, 333, 4, 5, 666, 7, 8, 9)),
            Contextual.resolvingByName(
                Where.and(lookupBy(foo, unresolved("x", TermType.NUMBER)), lookupBy(bar, unresolved("y", TermType.NUMBER))),
                row -> Map.of("x", row.get(0), "y", row.get(1)))
        );
        assertThat(updated).asList().containsExactly(1, 1, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666));
    }

    @Test
    public void update_data_where_batch_incomplete_columns_wrong_order_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int[] updated = table.updateDataWhereBatch(
            new BatchEntityIntData(List.of(value, bar), IntArrayList.from(333, 2, 666, 5)),
            Contextual.resolvingByOrderedList(
                Where.of(lookupBy(bar, unresolved("x", TermType.NUMBER))),
                row -> List.of(row.get(1)))
        );
        assertThat(updated).asList().containsExactly(1, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(4, 5, 666));
    }

    @Test
    public void update_data_where_batch_multiple_rows_match() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(1, 2, 3)));

        int[] updated = table.updateDataWhereBatch(
            newIntsModelBatch(IntArrayList.from(1, 2, 333)),
            Contextual.resolvingByName(
                Where.of(lookupBy(foo, unresolved("x", TermType.NUMBER))),
                row -> Map.of("x", row.get(0)))
        );
        assertThat(updated).asList().containsExactly(2);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333), new IntsModel(1, 2, 333));
    }

    /** {@link IntsModelTable#updateWhereOrInsert(Object, Where)} **/

    @Test
    public void update_where_or_insert_updated() {
        table.insert(new IntsModel(1, 2, 3));

        int affected = table.updateWhereOrInsert(
            new IntsModel(1, 2, 333),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333));
    }

    @Test
    public void update_where_or_insert_inserted() {
        table.insert(new IntsModel(1, 2, 3));

        int affected = table.updateWhereOrInsert(
            new IntsModel(111, 222, 333),
            Where.and(lookupBy(foo, var(111)), lookupBy(bar, var(222)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(111, 222, 333));
    }

    /** {@link IntsModelTable#updateWhereOrInsertData(EntityData, Where)} **/

    @Test
    public void update_where_or_insert_data_complete_updated() {
        table.insert(new IntsModel(1, 2, 3));

        int affected = table.updateWhereOrInsertData(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333));
    }

    @Test
    public void update_where_or_insert_data_complete_inserted() {
        table.insert(new IntsModel(1, 2, 3));

        int affected = table.updateWhereOrInsertData(
            newIntsModelData(IntArrayList.from(111, 222, 333)),
            Where.and(lookupBy(foo, var(111)), lookupBy(bar, var(222)))
        );
        assertEquals(affected, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(111, 222, 333));
    }

    /** {@link IntsModelTable#deleteWhere(Where)} **/

    @Test
    public void delete_where_by_id_found() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int deleted = table.deleteWhere(Where.of(lookupBy(foo, 1)));
        assertEquals(deleted, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(4, 5, 6));
    }

    @Test
    public void delete_where_by_id_not_found() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6)));

        int deleted = table.deleteWhere(Where.of(lookupBy(foo, 111)));
        assertEquals(deleted, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3), new IntsModel(4, 5, 6));
    }

    @Test
    public void delete_where_by_id_several_rows_match() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3), new IntsModel(111, 222, 3)));

        int deleted = table.deleteWhere(Where.of(lookupBy(value, 3)));
        assertEquals(deleted, 2);
        assertThat(table.fetchAll()).isEmpty();
    }
}
