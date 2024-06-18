package io.webby.demo.model;

import io.webby.auth.session.DefaultSession;
import io.webby.auth.session.SessionTable;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserTable;
import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.*;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.google.common.truth.Truth.assertThat;

public class BridgeIntLongModelTableTest
        extends SqlDbTableTest<BridgeIntLongModel, BridgeIntLongModelTable>
        implements BridgeTableTest<Integer, DefaultUser, Long, DefaultSession, BridgeIntLongModel, BridgeIntLongModelTable> {
    private UserTable users;
    private SessionTable sessions;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        users = new UserTable(connector);
        sessions = new SessionTable(connector);
        table = new BridgeIntLongModelTable(connector);
        users.admin().createTable(CreateTableQuery.of(users).ifNotExists());
        sessions.admin().createTable(CreateTableQuery.of(sessions).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull Pair<Integer[], DefaultUser[]> prepareLefts(int num) {
        return insertUsers(num);
    }

    @Override
    public @NotNull Pair<Long[], DefaultSession[]> prepareRights(int num) {
        return insertSessions(num);
    }

    @Override
    public void prepareRelations(@NotNull List<Pair<Integer, Long>> relations) {
        for (Pair<Integer, Long> relation : relations) {
            BridgeIntLongModel model = new BridgeIntLongModel(LongAutoIdModel.AUTO_ID,
                                                              ForeignInt.ofId(relation.first()),
                                                              ForeignLong.ofId(relation.second()),
                                                              relation.first() > relation.second());
            assertThat(table.insertAutoIncPk(model) > 0).isTrue();
        }
    }

    private @NotNull Pair<Integer[], DefaultUser[]> insertUsers(int num) {
        Integer[] keys = IntStream.range(1, num + 1).boxed().toArray(Integer[]::new);
        DefaultUser[] entities = IntStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(users, UserBuilder.simple(i)))
            .toArray(DefaultUser[]::new);
        return Pair.of(keys, entities);
    }

    private @NotNull Pair<Long[], DefaultSession[]> insertSessions(int num) {
        Long[] keys = LongStream.range(1, num + 1).boxed().toArray(Long[]::new);
        DefaultSession[] entities = LongStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(sessions, SessionBuilder.simple(i)))
            .toArray(DefaultSession[]::new);
        return Pair.of(keys, entities);
    }
}
