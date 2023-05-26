package io.webby.demo.model;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.BridgeTableTest;
import io.webby.testing.SessionBuilder;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.LongStream;

import static com.google.common.truth.Truth.assertThat;

public class BridgeLongModelTableTest
        extends SqlDbTableTest<BridgeLongModel, BridgeLongModelTable>
        implements BridgeTableTest<Long, DefaultSession, Long, DefaultSession, BridgeLongModel, BridgeLongModelTable> {
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
    public @NotNull Pair<Long[], DefaultSession[]> prepareLefts(int num) {
        return insertSessions(num);
    }

    @Override
    public @NotNull Pair<Long[], DefaultSession[]> prepareRights(int num) {
        return insertSessions(num);
    }

    @Override
    public void prepareRelations(@NotNull List<Pair<Long, Long>> relations) {
        for (Pair<Long, Long> relation : relations) {
            BridgeLongModel model = new BridgeLongModel(ForeignLong.ofId(relation.first()), ForeignLong.ofId(relation.second()));
            assertThat(table.insert(model)).isEqualTo(1);
        }
    }

    private @NotNull Pair<Long[], DefaultSession[]> insertSessions(int num) {
        Long[] keys = LongStream.range(1, num + 1).boxed().toArray(Long[]::new);
        DefaultSession[] entities = LongStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(sessions, SessionBuilder.simple(i)))
            .toArray(DefaultSession[]::new);
        return Pair.of(keys, entities);
    }
}
