package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapperModelTableTest
        extends SqlDbTableTest<MapperModel, MapperModelTable>
        implements TableIntTest<MapperModel, MapperModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new MapperModelTable(connector);
        table.admin().createTable(table.meta()).ifNotExists().run();
    }

    @Override
    public @NotNull MapperModel createEntity(@NotNull Integer key, int version) {
        return new MapperModel(key,
                               List.of(String.valueOf(key), String.valueOf(key + 1)),
                               Pair.of((long) version, (long) version));
    }

    @Override
    public @NotNull MapperModel copyEntityWithId(@NotNull MapperModel entity, int autoId) {
        return new MapperModel(autoId, entity.path(), entity.pair());
    }
}
