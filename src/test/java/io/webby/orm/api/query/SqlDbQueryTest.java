package io.webby.orm.api.query;

import io.webby.orm.api.debug.DebugSql;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static io.webby.orm.api.query.CompareType.EQ;
import static io.webby.orm.api.query.Shortcuts.STAR;
import static io.webby.orm.api.query.Shortcuts.literal;
import static io.webby.orm.testing.AssertSql.assertRepr;
import static io.webby.orm.testing.AssertSql.assertRows;
import static io.webby.orm.testing.PersonTableData.*;
import static io.webby.testing.TestingUtil.array;

@Tag("sql")
public class SqlDbQueryTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    @BeforeAll
    static void beforeAll() {
        SQL_DB.runUpdate("DROP TABLE IF EXISTS person");
        SQL_DB.runUpdate(SqlSchemaMaker.makeCreateTableQuery(SQL_DB.engine(), PERSON_META));

        List<Object[]> rows = List.of(
            array("Kate", "DE", FEMALE, parseDate("1990-01-01"), 110, 160.5, photo(0x1111111)),
            array("Bill", "US", MALE,   parseDate("1971-10-20"), 120, 170.1, photo(0x2222222)),
            array("Ivan", "RU", MALE,   parseDate("1999-07-16"), 100, 175.9, photo(0x3333333)),
            array("Yuan", "CN", FEMALE, parseDate("2005-05-31"), 130, 156.2, photo(0x4444444))
        );
        for (Object[] row : rows) {
            SQL_DB.runUpdate("""
                INSERT INTO person (name, country, sex, birthday, iq, height, photo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """, row);
        }
    }

    @Test
    public void selectWhere_id_by_name_const() {
        SelectQuery query = SelectWhere.of(SelectFrom.of(PersonColumn.id),
                                           Where.of(EQ.compare(PersonColumn.name, literal("Bill"))));
        assertRows(runQuery(query), array(2));
        assertRepr(query, """
            SELECT id
            FROM person
            WHERE name = 'Bill'
            """);
    }

    @Test
    public void groupBy_one_column_count() {
        SelectQuery query = SelectGroupBy.of(PersonColumn.sex, Func.COUNT.apply(STAR));
        assertRows(runQuery(query),
                   array(FEMALE, 2),
                   array(MALE, 2));
        assertRepr(query, """
            SELECT sex, count(*)
            FROM person
            GROUP BY sex
            """);
    }

    @Test
    public void groupBy_two_columns_max() {
        SelectQuery query = SelectGroupBy.of(PersonColumn.sex, PersonColumn.name, Func.MAX.apply(PersonColumn.id));
        assertRows(runQuery(query),
                   array(FEMALE, "Kate", 1),
                   array(MALE,   "Bill", 2),
                   array(MALE,   "Ivan", 3),
                   array(FEMALE, "Yuan", 4));
        assertRepr(query, """
            SELECT sex, name, max(id)
            FROM person
            GROUP BY sex, name
            """);
    }

    private @NotNull List<DebugSql.Row> runQuery(SelectQuery query) {
        return SQL_DB.runQuery(query.withTable(PERSON_META));
    }
}
