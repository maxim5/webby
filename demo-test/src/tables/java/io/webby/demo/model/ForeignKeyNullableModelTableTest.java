package io.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.api.query.CreateTableQuery;
import io.webby.testing.ForeignKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class ForeignKeyNullableModelTableTest
        extends SqlDbTableTest<ForeignKeyModel.Nullable, ForeignKeyNullableModelTable>
        implements TableIntTest<ForeignKeyModel.Nullable, ForeignKeyNullableModelTable>,
                   ForeignKeyTableTest<Integer, ForeignKeyModel.Nullable, ForeignKeyNullableModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ForeignKeyNullableModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(FKIntTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(FKLongTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(FKStringTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(ForeignKeyNullableModelTable.META).ifNotExists());
    }

    @Override
    protected void fillUp(@NotNull Connector connector) {
        // These contents support entity versions 0 and 1 (below).
        new FKIntTable(connector).insert(new ForeignKeyModel.InnerInt(1, 111));
        new FKLongTable(connector).insert(new ForeignKeyModel.InnerLong(1, 111_111));
        new FKStringTable(connector).insert(new ForeignKeyModel.InnerString("1", "value-1"));
    }

    @Override
    public int maxSupportedVersion() {
        return 1;
    }

    @Override
    public @NotNull ForeignKeyModel.Nullable createEntity(@NotNull Integer key, int version) {
        return version == 0 ?
            new ForeignKeyModel.Nullable(key, ForeignInt.empty(), ForeignLong.empty(), ForeignObj.empty()) :
            new ForeignKeyModel.Nullable(key, ForeignInt.ofId(version), ForeignLong.ofId(1), ForeignObj.ofId("1"));
    }

    @Override
    public @NotNull ForeignKeyModel.Nullable copyEntityWithId(@NotNull ForeignKeyModel.Nullable entity, int autoId) {
        return new ForeignKeyModel.Nullable(autoId, entity.innerInt(), entity.innerLong(), entity.innerString());
    }

    @Override
    public @NotNull ForeignKeyModel.Nullable enrichOneLevel(@NotNull ForeignKeyModel.Nullable model) {
        int intId = model.innerInt().getIntId();
        long longId = model.innerLong().getLongId();
        String strId = model.innerString().getFk();
        return new ForeignKeyModel.Nullable(
            model.id(),
            new ForeignInt<>(intId, intId == 0 ? null : new ForeignKeyModel.InnerInt(intId, intId * 111)),
            new ForeignLong<>(longId, longId == 0 ? null : new ForeignKeyModel.InnerLong(longId, longId * 111_111)),
            new ForeignObj<>(strId, strId == null ? null : new ForeignKeyModel.InnerString(strId, "value-" + strId))
        );
    }

    @Override
    public @NotNull ForeignKeyModel.Nullable enrichAllLevels(@NotNull ForeignKeyModel.Nullable model) {
        return enrichOneLevel(model);
    }
}
