package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class PrimitiveModelTableTest
        extends SqliteTableTest<Integer, PrimitiveModel, PrimitiveModelTable>
        implements TableIntTest<PrimitiveModel, PrimitiveModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE primitive_model (
                id INTEGER PRIMARY KEY,
                i INTEGER,
                l INTEGER,
                b INTEGER,
                s INTEGER,
                ch TEXT,
                f REAL,
                d REAL,
                bool INTEGER
            )
        """);
        table = new PrimitiveModelTable(connector);
    }

    @Override
    public @NotNull PrimitiveModel createEntity(@NotNull Integer key, int version) {
        return new PrimitiveModel(key, version, 1, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }

    @Override
    public @NotNull PrimitiveModel copyEntityWithId(@NotNull PrimitiveModel model, int autoId) {
        return new PrimitiveModel(autoId, model.i(), model.l(), model.b(), model.s(), model.ch(), model.f(), model.d(), model.bool());
    }
}
