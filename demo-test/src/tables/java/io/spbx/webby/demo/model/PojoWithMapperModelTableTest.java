package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.testing.SqlDbTableTest;
import io.spbx.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PojoWithMapperModelTableTest
        extends SqlDbTableTest<PojoWithMapperModel, PojoWithMapperModelTable>
        implements TableIntTest<PojoWithMapperModel, PojoWithMapperModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new PojoWithMapperModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull PojoWithMapperModel createEntity(@NotNull Integer key, int version) {
        return new PojoWithMapperModel(key, new PojoWithMapperModel.Pojo(new Point(version, version + 1)));
    }

    @Override
    public @NotNull PojoWithMapperModel copyEntityWithId(@NotNull PojoWithMapperModel entity, int autoId) {
        return new PojoWithMapperModel(autoId, entity.pojo());
    }
}
