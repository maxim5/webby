package io.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.util.base.Pair;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.auth.user.UserTable;
import io.spbx.webby.testing.UserBuilder;
import io.webby.testing.BridgeTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingSql;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;

public class BridgeIntModelTableTest
        extends SqlDbTableTest<BridgeIntModel, BridgeIntModelTable>
        implements BridgeTableTest<Integer, DefaultUser, Integer, DefaultUser, BridgeIntModel, BridgeIntModelTable> {
    private UserTable users;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        users = new UserTable(connector);
        table = new BridgeIntModelTable(connector);
        users.admin().createTable(CreateTableQuery.of(users).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
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
            BridgeIntModel model = new BridgeIntModel(ForeignInt.ofId(relation.first()), ForeignInt.ofId(relation.second()));
            assertThat(table.insert(model)).isEqualTo(1);
        }
    }

    private @NotNull Pair<Integer[], DefaultUser[]> insertUsers(int num) {
        Integer[] keys = IntStream.range(1, num + 1).boxed().toArray(Integer[]::new);
        DefaultUser[] entities = IntStream.range(1, num + 1)
            .mapToObj(i -> TestingSql.getOrInsert(users, UserBuilder.simple(i)))
            .toArray(DefaultUser[]::new);
        return Pair.of(keys, entities);
    }
}
