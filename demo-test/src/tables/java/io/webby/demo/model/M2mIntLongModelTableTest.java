package io.webby.demo.model;

import io.webby.auth.session.Session;
import io.webby.auth.session.SessionTable;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserTable;
import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import io.webby.testing.ManyToManyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class M2mIntLongModelTableTest
        extends SqlDbTableTest<M2mIntLongModel, M2mIntLongModelTable>
        implements ManyToManyTableTest<Integer, DefaultUser, Long, Session, M2mIntLongModel, M2mIntLongModelTable> {
    private UserTable users;
    private SessionTable sessions;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new M2mIntLongModelTable(connector);
        users = new UserTable(connector);
        sessions = new SessionTable(connector);
        table.admin().createTable(table.meta()).ifNotExists().run();
        users.admin().createTable(users.meta()).ifNotExists().run();
        sessions.admin().createTable(sessions.meta()).ifNotExists().run();
    }

    @Override
    public @NotNull Pair<Integer[], DefaultUser[]> prepareLefts(int num) {
        return insertUsers(num);
    }

    @Override
    public @NotNull Pair<Long[], Session[]> prepareRights(int num) {
        return insertSessions(num);
    }

    @Override
    public void prepareRelations(@NotNull List<Pair<Integer, Long>> relations) {
        for (Pair<Integer, Long> relation : relations) {
            M2mIntLongModel model = new M2mIntLongModel(LongAutoIdModel.AUTO_ID,
                                                        ForeignInt.ofId(relation.first()),
                                                        ForeignLong.ofId(relation.second()),
                                                        relation.first() > relation.second());
            assertTrue(table.insertAutoIncPk(model) > 0);
        }
    }

    private @NotNull Pair<Integer[], DefaultUser[]> insertUsers(int num) {
        Integer[] keys = IntStream.range(1, num + 1).boxed().toArray(Integer[]::new);
        DefaultUser[] entities = IntStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(users, TestingModels.newUserNowFixMillis(i)))
            .toArray(DefaultUser[]::new);
        return Pair.of(keys, entities);
    }

    private @NotNull Pair<Long[], Session[]> insertSessions(int num) {
        Long[] keys = LongStream.range(1, num + 1).boxed().toArray(Long[]::new);
        Session[] entities = LongStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(sessions, TestingModels.newSessionNowFixMillis(i)))
            .toArray(Session[]::new);
        return Pair.of(keys, entities);
    }
}
