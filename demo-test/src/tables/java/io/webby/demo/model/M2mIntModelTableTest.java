package io.webby.demo.model;

import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignInt;
import io.webby.testing.ManyToManyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class M2mIntModelTableTest
        extends SqlDbTableTest<M2mIntModel, M2mIntModelTable>
        implements ManyToManyTableTest<Integer, DefaultUser, Integer, DefaultUser, M2mIntModel, M2mIntModelTable> {
    private UserTable users;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new M2mIntModelTable(connector);
        users = new UserTable(connector);
        table.admin().createTableIfNotExists(table.meta());
        users.admin().createTableIfNotExists(users.meta());
    }

    @Override
    public @NotNull Pair<Integer[], DefaultUser[]> prepareLefts(int num) {
        return insertUsers(num);
    }

    @Override
    public @NotNull Pair<Integer[], DefaultUser[]> prepareRights(int num) {
        return insertUsers(num);
    }

    @Override
    public void prepareRelations(@NotNull List<Pair<Integer, Integer>> relations) {
        for (Pair<Integer, Integer> relation : relations) {
            M2mIntModel model = new M2mIntModel(ForeignInt.ofId(relation.first()), ForeignInt.ofId(relation.second()));
            assertEquals(1, table.insert(model));
        }
    }

    private @NotNull Pair<Integer[], DefaultUser[]> insertUsers(int num) {
        Integer[] keys = IntStream.range(1, num + 1).boxed().toArray(Integer[]::new);
        DefaultUser[] entities = IntStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(users, TestingModels.newUserNowFixMillis(i)))
            .toArray(DefaultUser[]::new);
        return Pair.of(keys, entities);
    }
}
