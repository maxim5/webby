package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.testing.SqlDbTableTest;
import io.spbx.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OptionalModelTableTest
        extends SqlDbTableTest<OptionalModel, OptionalModelTable>
        implements TableIntTest<OptionalModel, OptionalModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new OptionalModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull OptionalModel createEntity(@NotNull Integer key, int version) {
        return new OptionalModel(key,
                                 version == 0 ? Optional.empty() : Optional.of(version),
                                 Optional.empty(),
                                 Optional.of(String.valueOf(version)));
    }

    @Override
    public @NotNull OptionalModel copyEntityWithId(@NotNull OptionalModel entity, int autoId) {
        return new OptionalModel(autoId, entity.i(), entity.l(), entity.str());
    }
}
