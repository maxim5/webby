package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// TODO: complex primary keys aren't supported right now (ComplexIdModel, needs tests in all DBs).
public class ComplexIdModelTableTest
        extends SqlDbTableTest<ComplexIdModel, ComplexIdModelTable>
        implements PrimaryKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ComplexIdModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull ComplexIdModel.Key[] keys() {
        return array(new ComplexIdModel.Key(1, 1, "1"), new ComplexIdModel.Key(2, 2, "2"), new ComplexIdModel.Key(3, 3, "3"));
    }

    @Override
    public @NotNull ComplexIdModel createEntity(ComplexIdModel.@NotNull Key key, int version) {
        return new ComplexIdModel(key, version);
    }

    @Override
    public void fetch_all_matching_entities() {
        assumeTrue(false, "Complex ids don't generate a PK");
    }

    @Override
    public void get_first_matching_entity() {
        assumeTrue(false, "Complex ids don't generate a PK");
    }

    @Override
    public void insert_entity_already_exists_throws() {
        assumeTrue(false, "Complex ids are not unique, hence allow duplicate inserts");
    }

    @Override
    public void insert_batch_of_two_duplicates() {
        assumeTrue(false, "Complex ids are not unique, hence allow duplicate inserts");
    }

    @Override
    public void insert_ignore() {
        assumeTrue(false, "Complex ids are not unique, hence allow duplicate inserts");
    }
}
