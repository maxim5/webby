package io.webby.demo.model;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.orm.api.Connector;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.BitSet;
import java.util.List;

public class MapperModelTableTest
        extends SqlDbTableTest<MapperModel, MapperModelTable>
        implements TableIntTest<MapperModel, MapperModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new MapperModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull MapperModel createEntity(@NotNull Integer key, int version) {
        return new MapperModel(key,
                               List.of(String.valueOf(key), String.valueOf(key + 1)),
                               Pair.of((long) version, (long) version),
                               IntArrayList.from(version),
                               Instant.ofEpochMilli(1_000_000_000_000L),
                               BitSet.valueOf(new long[] { version + 31 }));
    }

    @Override
    public @NotNull MapperModel copyEntityWithId(@NotNull MapperModel entity, int autoId) {
        return new MapperModel(autoId, entity.path(), entity.pair(), entity.ints(), entity.time(), entity.bits());
    }
}
