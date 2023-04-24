package io.webby.demo.model;

import io.webby.auth.session.Session;
import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.BridgeTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BridgeLongModelTableTest
        extends SqlDbTableTest<BridgeLongModel, BridgeLongModelTable>
        implements BridgeTableTest<Long, Session, Long, Session, BridgeLongModel, BridgeLongModelTable> {
    private SessionTable sessions;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        UserTable users = new UserTable(connector);
        sessions = new SessionTable(connector);
        table = new BridgeLongModelTable(connector);
        users.admin().createTable(CreateTableQuery.of(users).ifNotExists());
        sessions.admin().createTable(CreateTableQuery.of(sessions).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
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
            BridgeLongModel model = new BridgeLongModel(ForeignLong.ofId(relation.first()), ForeignLong.ofId(relation.second()));
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
