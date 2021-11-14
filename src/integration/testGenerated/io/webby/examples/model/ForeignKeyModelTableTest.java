package io.webby.examples.model;

import io.webby.testing.BaseModelForeignKeyTableTest;
import io.webby.util.sql.api.ForeignInt;
import io.webby.util.sql.api.ForeignLong;
import io.webby.util.sql.api.ForeignObj;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class ForeignKeyModelTableTest extends BaseModelForeignKeyTableTest<Long, ForeignKeyModel, ForeignKeyModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_int (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_long (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_string (
                id TEXT PRIMARY KEY,
                value TEXT
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE foreign_key_model (
                id INTEGER PRIMARY KEY,
                inner_int_id INTEGER,
                inner_long_id INTEGER,
                inner_string_id TEXT
            )
        """);
        keys = array(1L, 2L);
        table = new ForeignKeyModelTable(connection);
    }

    @Override
    protected @NotNull ForeignKeyModel createEntity(@NotNull Long key, int version) {
        return new ForeignKeyModel(key, ForeignInt.ofId(version), ForeignLong.ofId(777), ForeignObj.ofId("foo"));
    }

    @Override
    protected @NotNull ForeignKeyModel enrichOneLevel(@NotNull ForeignKeyModel model) {
        ForeignKeyModel.InnerInt fkInt = new ForeignKeyModel.InnerInt(model.innerInt().getIntId(), 123);
        new FKIntTable(connection).insert(fkInt);

        ForeignKeyModel.InnerLong fkLong = new ForeignKeyModel.InnerLong(model.innerLong().getLongId(), 456);
        new FKLongTable(connection).insert(fkLong);

        ForeignKeyModel.InnerString fkString = new ForeignKeyModel.InnerString(model.innerString().getFk(), "foobar");
        new FKStringTable(connection).insert(fkString);

        return new ForeignKeyModel(
            model.id(),
            ForeignInt.ofEntity(fkInt.id(), fkInt),
            ForeignLong.ofEntity(fkLong.id(), fkLong),
            ForeignObj.ofEntity(fkString.id(), fkString)
        );
    }

    @Override
    protected @NotNull ForeignKeyModel enrichAllLevels(@NotNull ForeignKeyModel model) {
        return enrichOneLevel(model);
    }
}
