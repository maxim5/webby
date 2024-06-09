package io.webby.demo.model;

import com.google.common.primitives.Ints;
import io.webby.demo.model.StringModel.StringDuo;
import io.webby.orm.api.Connector;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.orm.api.query.SelectWhere;
import io.webby.testing.MaliciousTableTest;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBasics.array;

public class StringModelTableTest extends SqlDbTableTest<StringModel, StringModelTable>
        implements PrimaryKeyTableTest<String, StringModel, StringModelTable>,
                   MaliciousTableTest<String, StringModel, StringModelTable> {
    private static final SelectWhere SELECT_DUO_QUERY = SelectWhere.from(StringModelTable.META)
        .select(StringModelTable.OwnColumn.id, StringModelTable.OwnColumn.chars)
        .orderBy(StringModelTable.OwnColumn.id)
        .build();

    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new StringModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull String[] keys() {
        return array("foo", "bar", "baz");
    }

    @Override
    public @NotNull String[] maliciousKeys() {
        return SqlInjections.MALICIOUS_STRING_INPUTS;
    }

    @Override
    public @NotNull StringModel createEntity(@NotNull String key, int version) {
        return new StringModel(key, key, key.toCharArray(), Ints.toByteArray(version));
    }

    @Test
    public void select_duos() {
        table.insertBatch(List.of(createEntity("1", 111), createEntity("2", 222)));
        List<StringDuo> expected = List.of(new StringDuo("1", "1"), new StringDuo("2", "2"));

        StringDuo one = runner().runAndGet(SELECT_DUO_QUERY, StringModel_StringDuo_JdbcAdapter.ADAPTER);
        assertThat(one).isEqualTo(expected.getFirst());

        List<StringDuo> fetched = runner().fetchAll(SELECT_DUO_QUERY, StringModel_StringDuo_JdbcAdapter.ADAPTER);
        assertThat(fetched).containsExactlyElementsIn(expected).inOrder();

        List<StringDuo> iterated = new ArrayList<>();
        runner().forEach(SELECT_DUO_QUERY, StringModel_StringDuo_JdbcAdapter.ADAPTER.via(iterated::add));
        assertThat(iterated).containsExactlyElementsIn(expected).inOrder();
    }
}
