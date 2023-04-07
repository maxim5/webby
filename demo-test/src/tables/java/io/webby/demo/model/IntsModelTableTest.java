package io.webby.demo.model;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.orm.api.Connector;
import io.webby.orm.api.entity.BatchEntityIntData;
import io.webby.orm.api.entity.EntityIntData;
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
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Test
    public void invalid_params_throws() {
        assertThrows(AssertionError.class, () -> newIntsModelData(IntArrayList.from(1, 2)));
        assertThrows(AssertionError.class, () -> newIntsModelBatch(IntArrayList.from(1, 2, 3, 4)));
    }

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

    @Test
    public void update_data_where_complete_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.and(lookupBy(foo, var(1)), lookupBy(bar, var(2)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 333));
    }

    @Test
    public void update_data_where_incomplete_one_column_ok() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            new EntityIntData(List.of(bar), IntArrayList.from(777)),
            Where.of(lookupBy(foo, var(1)))
        );
        assertEquals(updated, 1);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 777, 3));
    }

    @Test
    public void update_data_where_not_found() {
        table.insertBatch(List.of(new IntsModel(1, 2, 3)));

        int updated = table.updateDataWhere(
            newIntsModelData(IntArrayList.from(1, 2, 333)),
            Where.of(lookupBy(foo, var(111)))
        );
        assertEquals(updated, 0);
        assertThat(table.fetchAll()).containsExactly(new IntsModel(1, 2, 3));
    }

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
}
