package io.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.orm.api.query.SelectWhere;
import io.webby.demo.model.PrimitiveModel.PrimitiveDuo;
import io.webby.demo.model.PrimitiveModel.PrimitiveTrio;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class PrimitiveModelTableTest
        extends SqlDbTableTest<PrimitiveModel, PrimitiveModelTable>
        implements TableIntTest<PrimitiveModel, PrimitiveModelTable> {
    private static final SelectWhere SELECT_DUO_QUERY = SelectWhere.from(PrimitiveModelTable.META)
        .select(PrimitiveModelTable.OwnColumn.i, PrimitiveModelTable.OwnColumn.l)
        .orderBy(PrimitiveModelTable.OwnColumn.id)
        .build();
    private static final SelectWhere SELECT_TRIO_QUERY = SelectWhere.from(PrimitiveModelTable.META)
        .select(PrimitiveModelTable.OwnColumn.b, PrimitiveModelTable.OwnColumn.s, PrimitiveModelTable.OwnColumn.ch)
        .orderBy(PrimitiveModelTable.OwnColumn.id)
        .build();

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new PrimitiveModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull PrimitiveModel createEntity(@NotNull Integer key, int version) {
        return new PrimitiveModel(key, version, 1, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }

    @Override
    public @NotNull PrimitiveModel copyEntityWithId(@NotNull PrimitiveModel model, int autoId) {
        return new PrimitiveModel(autoId, model.i(), model.l(), model.b(), model.s(), model.ch(), model.f(), model.d(), model.bool());
    }

    @Test
    public void select_duos() {
        table.insertBatch(List.of(createEntity(1, 111), createEntity(2, 222)));
        List<PrimitiveDuo> expected = List.of(new PrimitiveDuo(111, 1), new PrimitiveDuo(222, 1));

        PrimitiveDuo one = runner().runAndGet(SELECT_DUO_QUERY, PrimitiveModel_PrimitiveDuo_JdbcAdapter.ADAPTER);
        assertThat(one).isEqualTo(expected.getFirst());

        List<PrimitiveDuo> fetched = runner().fetchAll(SELECT_DUO_QUERY, PrimitiveModel_PrimitiveDuo_JdbcAdapter.ADAPTER);
        assertThat(fetched).containsExactlyElementsIn(expected).inOrder();

        List<PrimitiveDuo> iterated = new ArrayList<>();
        runner().forEach(SELECT_DUO_QUERY, PrimitiveModel_PrimitiveDuo_JdbcAdapter.ADAPTER.via(iterated::add));
        assertThat(iterated).containsExactlyElementsIn(expected).inOrder();
    }

    @Test
    public void select_trios() {
        table.insertBatch(List.of(createEntity(1, 777)));
        List<PrimitiveTrio> expected = List.of(new PrimitiveTrio((byte) 2, (short) 3, 'M'));

        PrimitiveTrio one = runner().runAndGet(SELECT_TRIO_QUERY, PrimitiveModel_PrimitiveTrio_JdbcAdapter.ADAPTER);
        assertThat(one).isEqualTo(expected.getFirst());

        List<PrimitiveTrio> fetched = runner().fetchAll(SELECT_TRIO_QUERY, PrimitiveModel_PrimitiveTrio_JdbcAdapter.ADAPTER);
        assertThat(fetched).containsExactlyElementsIn(expected).inOrder();

        List<PrimitiveTrio> iterated = new ArrayList<>();
        runner().forEach(SELECT_TRIO_QUERY, PrimitiveModel_PrimitiveTrio_JdbcAdapter.ADAPTER.via(iterated::add));
        assertThat(iterated).containsExactlyElementsIn(expected).inOrder();
    }
}
