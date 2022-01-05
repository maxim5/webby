package io.webby.demo.model;

import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ManyToManyTableTest;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingSql;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class M2mIntModelTableTest
        extends SqlDbTableTest<M2mIntModel, M2mIntModelTable>
        implements ManyToManyTableTest<Integer, DefaultUser, Integer, DefaultUser, M2mIntModel, M2mIntModelTable> {
    private UserTable users;

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new M2mIntModelTable(connector);
        users = new UserTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table.engine(), M2mIntModelTable.class));
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(users.engine(), UserTable.class));
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
        Integer[] keys = new Integer[num];
        DefaultUser[] entities = new DefaultUser[num];
        for (int i = 0; i < num; i++) {
            keys[i] = i + 1;
            entities[i] = TestingSql.getOrInsert(users, TestingModels.newUserNowMillisFix(i + 1));
        }
        return Pair.of(keys, entities);
    }
}
