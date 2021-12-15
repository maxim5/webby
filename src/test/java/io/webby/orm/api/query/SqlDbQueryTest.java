package io.webby.orm.api.query;

import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static io.webby.orm.api.query.CompareType.EQ;
import static io.webby.orm.api.query.CompareType.GT;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.testing.AssertSql.*;
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
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.id)
                .where(Where.of(EQ.compare(PersonColumn.name, literal("Bill"))))
                .build();
        assertRepr(query, """
            SELECT id
            FROM person
            WHERE name = 'Bill'
            """);
        assertNoArgs(query);
        assertRows(SQL_DB.runQuery(query), array(2));
    }

    @Test
    public void selectWhere_select_arg() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.name, GT.compare(PersonColumn.iq, var(125)))
                .build();
        assertRepr(query, """
            SELECT name, iq > ?
            FROM person
            """);
        assertArgs(query, 125);
        assertRows(SQL_DB.runQuery(query),
                   array("Kate", false),
                   array("Bill", false),
                   array("Ivan", false),
                   array("Yuan", true));
    }

    @Test
    public void selectWhere_where_arg() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.id, PersonColumn.country)
                .where(Where.and(EQ.compare(Func.LENGTH.apply(PersonColumn.name), var(4)),
                                 like(PersonColumn.country, var("%U%"))))
                .build();
        assertRepr(query, """
            SELECT id, country
            FROM person
            WHERE length(name) = ? AND country LIKE ?
            """);
        assertArgs(query, 4, "%U%");
        assertRows(SQL_DB.runQuery(query),
                   array(2, "US"),
                   array(3, "RU"));
    }

    @Test
    public void groupBy_one_column_count() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
                .groupBy(PersonColumn.sex)
                .aggregate(Func.COUNT.apply(STAR))
                .build();
        assertRepr(query, """
            SELECT sex, count(*)
            FROM person
            GROUP BY sex
            """);
        assertNoArgs(query);
        assertRows(SQL_DB.runQuery(query),
                   array(FEMALE, 2),
                   array(MALE, 2));
    }

    @Test
    public void groupBy_two_columns_max() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
                .groupBy(PersonColumn.sex, PersonColumn.name)
                .aggregate(Func.MAX.apply(PersonColumn.id))
                .build();
        assertRepr(query, """
            SELECT sex, name, max(id)
            FROM person
            GROUP BY sex, name
            """);
        assertNoArgs(query);
        assertRows(SQL_DB.runQuery(query),
                   array(FEMALE, "Kate", 1),
                   array(MALE,   "Bill", 2),
                   array(MALE,   "Ivan", 3),
                   array(FEMALE, "Yuan", 4));
    }
}
