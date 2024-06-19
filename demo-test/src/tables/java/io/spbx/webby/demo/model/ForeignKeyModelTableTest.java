package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.testing.ForeignKeyTableTest;
import io.spbx.webby.testing.SqlDbTableTest;
import io.spbx.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

public class ForeignKeyModelTableTest
        extends SqlDbTableTest<ForeignKeyModel, ForeignKeyModelTable>
        implements TableLongTest<ForeignKeyModel, ForeignKeyModelTable>,
                   ForeignKeyTableTest<Long, ForeignKeyModel, ForeignKeyModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ForeignKeyModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(FKIntTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(FKLongTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(FKStringTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(ForeignKeyModelTable.META).ifNotExists());
    }

    @Override
    protected void fillUp(@NotNull Connector connector) {
        // These contents support entity versions 0 and 1 (below).
        new FKIntTable(connector).insert(new ForeignKeyModel.InnerInt(1, 111));
        new FKIntTable(connector).insert(new ForeignKeyModel.InnerInt(2, 222));
        new FKLongTable(connector).insert(new ForeignKeyModel.InnerLong(7, 777_777));
        new FKStringTable(connector).insert(new ForeignKeyModel.InnerString("555", "value-555"));
    }

    @Override
    public int maxSupportedVersion() {
        return 1;
    }

    @Override
    public @NotNull ForeignKeyModel createEntity(@NotNull Long key, int version) {
        return new ForeignKeyModel(key, ForeignInt.ofId(version + 1), ForeignLong.ofId(7), ForeignObj.ofId("555"));
    }

    @Override
    public @NotNull ForeignKeyModel copyEntityWithId(@NotNull ForeignKeyModel entity, long autoId) {
        return new ForeignKeyModel(autoId, entity.innerInt(), entity.innerLong(), entity.innerString());
    }

    @Override
    public @NotNull ForeignKeyModel enrichOneLevel(@NotNull ForeignKeyModel model) {
        int intId = model.innerInt().getIntId();
        long longId = model.innerLong().getLongId();
        String strId = model.innerString().getFk();
        return new ForeignKeyModel(
            model.id(),
            new ForeignInt<>(intId, new ForeignKeyModel.InnerInt(intId, intId * 111)),
            new ForeignLong<>(longId, new ForeignKeyModel.InnerLong(longId, longId * 111_111)),
            new ForeignObj<>(strId, new ForeignKeyModel.InnerString(strId, "value-" + strId))
        );
    }

    @Override
    public @NotNull ForeignKeyModel enrichAllLevels(@NotNull ForeignKeyModel model) {
        return enrichOneLevel(model);
    }
}
