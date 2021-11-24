package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.ForeignKeyTableTest;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableLongTest;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.ForeignObj;
import org.jetbrains.annotations.NotNull;

public class ForeignKeyModelTableTest
        extends SqliteTableTest<Long, ForeignKeyModel, ForeignKeyModelTable>
        implements TableLongTest<ForeignKeyModel, ForeignKeyModelTable>,
                   ForeignKeyTableTest<Long, ForeignKeyModel, ForeignKeyModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE f_k_int (
                id INTEGER PRIMARY KEY,
                value INTEGER
            );
            CREATE TABLE f_k_long (
                id INTEGER PRIMARY KEY,
                value INTEGER
            );
            CREATE TABLE f_k_string (
                id TEXT PRIMARY KEY,
                value TEXT
            );
            CREATE TABLE foreign_key_model (
                id INTEGER PRIMARY KEY,
                inner_int_id INTEGER,
                inner_long_id INTEGER,
                inner_string_id TEXT
            );
        """);
        table = new ForeignKeyModelTable(connector);
    }

    @Override
    public @NotNull ForeignKeyModel createEntity(@NotNull Long key, int version) {
        return new ForeignKeyModel(key, ForeignInt.ofId(version), ForeignLong.ofId(777), ForeignObj.ofId("foo"));
    }

    @Override
    public @NotNull ForeignKeyModel copyEntityWithId(@NotNull ForeignKeyModel entity, long autoId) {
        return new ForeignKeyModel(autoId, entity.innerInt(), entity.innerLong(), entity.innerString());
    }

    @Override
    public @NotNull ForeignKeyModel enrichOneLevel(@NotNull ForeignKeyModel model) {
        ForeignKeyModel.InnerInt fkInt = new ForeignKeyModel.InnerInt(model.innerInt().getIntId(), 123);
        new FKIntTable(connector()).insert(fkInt);

        ForeignKeyModel.InnerLong fkLong = new ForeignKeyModel.InnerLong(model.innerLong().getLongId(), 456);
        new FKLongTable(connector()).insert(fkLong);

        ForeignKeyModel.InnerString fkString = new ForeignKeyModel.InnerString(model.innerString().getFk(), "foobar");
        new FKStringTable(connector()).insert(fkString);

        return new ForeignKeyModel(
            model.id(),
            ForeignInt.ofEntity(fkInt.id(), fkInt),
            ForeignLong.ofEntity(fkLong.id(), fkLong),
            ForeignObj.ofEntity(fkString.id(), fkString)
        );
    }

    @Override
    public @NotNull ForeignKeyModel enrichAllLevels(@NotNull ForeignKeyModel model) {
        return enrichOneLevel(model);
    }
}
