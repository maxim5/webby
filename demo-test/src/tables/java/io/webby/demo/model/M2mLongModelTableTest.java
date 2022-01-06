package io.webby.demo.model;

import io.webby.auth.session.Session;
import io.webby.auth.session.SessionTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ManyToManyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class M2mLongModelTableTest
        extends SqlDbTableTest<M2mLongModel, M2mLongModelTable>
        implements ManyToManyTableTest<Long, Session, Long, Session, M2mLongModel, M2mLongModelTable> {
    private SessionTable sessions;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new M2mLongModelTable(connector);
        sessions = new SessionTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(sessions));
    }

    @Override
    public @NotNull Pair<Long[], Session[]> prepareLefts(int num) {
        return insertSessions(num);
    }

    @Override
    public @NotNull Pair<Long[], Session[]> prepareRights(int num) {
        return insertSessions(num);
    }

    @Override
    public void prepareRelations(@NotNull List<Pair<Long, Long>> relations) {
        for (Pair<Long, Long> relation : relations) {
            M2mLongModel model = new M2mLongModel(ForeignLong.ofId(relation.first()), ForeignLong.ofId(relation.second()));
            assertEquals(1, table.insert(model));
        }
    }

    private @NotNull Pair<Long[], Session[]> insertSessions(int num) {
        Long[] keys = LongStream.range(1, num + 1).boxed().toArray(Long[]::new);
        Session[] entities = LongStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(sessions, TestingModels.newSessionNowFixMillis(i)))
            .toArray(Session[]::new);
        return Pair.of(keys, entities);
    }
}
