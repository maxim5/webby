package io.webby.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import io.webby.orm.api.Engine;
import io.webby.testing.ext.SqlCleanupExtension;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static io.webby.orm.api.query.CompareType.*;
import static io.webby.orm.api.query.Func.*;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.testing.AssertSql.UnitSubject;
import static io.webby.orm.testing.AssertSql.assertRows;
import static io.webby.orm.testing.PersonTableData.*;
import static io.webby.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@Tag("sql")
public class SelectQueryTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties().disableSavepoints();
    @RegisterExtension private static final SqlCleanupExtension CLEANUP = SqlCleanupExtension.of(SQL, PERSON_META);

    @BeforeEach
    void setUp() {
        List<Object[]> rows = List.of(
            array(1, "Kate", "DE", FEMALE, parseDate("1990-01-01"), 110, 160.5, photo(0x1111111)),
            array(2, "Bill", "US", MALE,   parseDate("1971-10-20"), 120, 170.1, photo(0x2222222)),
            array(3, "Ivan", "RU", MALE,   parseDate("1999-07-16"), 100, 175.9, photo(0x3333333)),
            array(4, "Yuan", "CN", FEMALE, parseDate("2005-05-31"), 130, 156.2, photo(0x4444444))
        );
        for (Object[] row : rows) {
            SQL.runUpdate("""
                INSERT INTO person (id, name, country, sex, birthday, iq, height, photo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, row);
        }
    }

    @Test
    public void selectWhere_select_id_by_name_const() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.id)
                .where(Where.of(EQ.compare(PersonColumn.name, literal("Bill"))))
                .build();
        assertThat(query).matches("""
            SELECT id
            FROM person
            WHERE name = 'Bill'
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(array(2));
    }

    @Test
    public void selectWhere_select_sum() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(SUM.apply(PersonColumn.height))
                .build();
        assertThat(query).matches("""
            SELECT sum(height)
            FROM person
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(array(662.7));
    }

    @Test
    public void selectWhere_select_distinct() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.sex.distinct())
                .build();
        assertThat(query).matches("""
            SELECT DISTINCT sex
            FROM person
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(FEMALE),
            array(MALE)
        );
    }

    @Test
    public void selectWhere_order_by_sex_country() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.sex, PersonColumn.country)
                .where(Where.of(LT.compare(PersonColumn.height, num(200.0))))
                .orderBy(OrderBy.of(PersonColumn.sex.ordered(Order.ASC), PersonColumn.country.ordered(Order.DESC)))
                .build();
        assertThat(query).matches("""
            SELECT sex, country
            FROM person
            WHERE height < 200.0
            ORDER BY sex ASC, country DESC
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(MALE, "US"),
            array(MALE, "RU"),
            array(FEMALE, "DE"),
            array(FEMALE, "CN")
        ).inOrder();
    }

    @Test
    public void selectWhere_select_arg() {
        SelectQuery query = SelectWhere.from(PERSON_META)
                .select(PersonColumn.name, GT.compare(PersonColumn.iq, var(125)))
                .build();
        assertThat(query).matches("""
            SELECT name, iq > ?
            FROM person
            """);
        assertThat(query).containsArgsExactly(125);
        assertRows(SQL.runQuery(query)).containsExactly(
            array("Kate", false),
            array("Bill", false),
            array("Ivan", false),
            array("Yuan", true)
        );
    }

    @Test
    public void selectWhere_where_arg() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, PersonColumn.country)
            .where(Where.and(
                EQ.compare(LENGTH.apply(PersonColumn.name), var(4)),
                like(PersonColumn.country, var("%U%"))
            )).build();
        assertThat(query).matches("""
            SELECT id, country
            FROM person
            WHERE length(name) = ? AND country LIKE ?
            """);
        assertThat(query).containsArgsExactly(4, "%U%");
        assertRows(SQL.runQuery(query)).containsExactly(
            array(2, "US"),
            array(3, "RU")
        );
    }

    @Test
    public void selectWhere_date_comparison() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, PersonColumn.country)
            .where(Where.of(LT.compare(PersonColumn.birthday, var(parseDate("1985-01-01")))))
            .build();
        assertThat(query).matches("""
            SELECT id, country
            FROM person
            WHERE birthday < ?
            """);
        assertThat(query).containsArgsExactly(parseDate("1985-01-01"));
        assertRows(SQL.runQuery(query)).containsExactly(array(2, "US"));
    }

    @Test
    public void selectWhere_date_between() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, PersonColumn.country)
            .where(Where.of(between(PersonColumn.birthday, var(parseDate("1985-01-01")), var(parseDate("2000-01-01")))))
            .build();
        assertThat(query).matches("""
            SELECT id, country
            FROM person
            WHERE birthday BETWEEN ? AND ?
            """);
        assertThat(query).containsArgsExactly(parseDate("1985-01-01"), parseDate("2000-01-01"));
        assertRows(SQL.runQuery(query)).containsExactly(
            array(1, "DE"),
            array(3, "RU")
        );
    }

    @Test
    public void selectWhere_ifNull() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, IFNULL_STR.apply(PersonColumn.country, literal("??")))
            .build();
        assertThat(query).matches("""
            SELECT id, ifnull(country, '??')
            FROM person
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(1, "DE"),
            array(2, "US"),
            array(3, "RU"),
            array(4, "CN")
        );
    }

    @Test
    public void selectWhere_cast_to_integer() {
        assumeFalse(SQL.engine() == Engine.H2, "Not working in H2 (blob cast doesn't work)");
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, CAST_AS_SIGNED.apply(PersonColumn.photo))
            .build();
        assertThat(query).matches("""
            SELECT id, CAST(photo AS SIGNED)
            FROM person
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(1, 1111111),
            array(2, 2222222),
            array(3, 3333333),
            array(4, 4444444)
        );
    }

    @Test
    public void selectWhere_cast_to_string() {
        assumeFalse(SQL.engine() == Engine.H2, "Not working in H2 (blob cast doesn't work)");
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id, CAST_AS_CHAR.apply(PersonColumn.photo))
            .build();
        assertThat(query).matches("""
            SELECT id, CAST(photo AS CHAR)
            FROM person
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(1, "1111111"),
            array(2, "2222222"),
            array(3, "3333333"),
            array(4, "4444444")
        );
    }

    @Test
    public void groupBy_one_column_count() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
            .groupBy(PersonColumn.sex)
            .aggregate(COUNT.apply(STAR))
            .build();
        assertThat(query).matches("""
            SELECT sex, count(*)
            FROM person
            GROUP BY sex
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(FEMALE, 2),
            array(MALE, 2)
        );
    }

    @Test
    public void groupBy_one_column_avg() {
        assumeFalse(SQL.engine() == Engine.H2, "Not working in H2 (AVG is integer by default)");
        SelectQuery query = SelectGroupBy.from(PERSON_META)
            .groupBy(PersonColumn.sex)
            .aggregate(AVG.apply(PersonColumn.iq))
            .build();
        assertThat(query).matches("""
            SELECT sex, avg(iq)
            FROM person
            GROUP BY sex
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(FEMALE, 120.0),
            array(MALE, 110.0)
        );
    }

    @Test
    public void groupBy_two_columns_max() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
            .groupBy(PersonColumn.sex, PersonColumn.name)
            .aggregate(MAX.apply(PersonColumn.id))
            .build();
        assertThat(query).matches("""
            SELECT sex, name, max(id)
            FROM person
            GROUP BY sex, name
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array(FEMALE, "Kate", 1),
            array(MALE, "Bill", 2),
            array(MALE, "Ivan", 3),
            array(FEMALE, "Yuan", 4)
        );
    }

    @Test
    public void groupBy_where_order_by() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
            .aggregate(COUNT.apply(PersonColumn.id))
            .where(Where.of(PersonColumn.sex.bool()))
            .groupBy(PersonColumn.name)
            .orderBy(OrderBy.of(PersonColumn.id, Order.ASC))
            .build();
        assertThat(query).matches("""
            SELECT name, count(id)
            FROM person
            WHERE sex
            GROUP BY name
            ORDER BY id ASC
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array("Kate", 1),
            array("Yuan", 1)
        ).inOrder();
    }

    @Test
    public void groupBy_having() {
        SelectQuery query = SelectGroupBy.from(PERSON_META)
            .aggregate(COUNT.apply(PersonColumn.id))
            .groupBy(PersonColumn.country)
            .having(Having.of(GT.compare(COUNT.apply(PersonColumn.id), ZERO)))
            .build();
        assertThat(query).matches("""
            SELECT country, count(id)
            FROM person
            GROUP BY country
            HAVING count(id) > 0
            """);
        assertThat(query).containsNoArgs();
        assertRows(SQL.runQuery(query)).containsExactly(
            array("CN", 1),
            array("DE", 1),
            array("RU", 1),
            array("US", 1)
        );
    }

    @Test
    public void runner_fetchIntColumn() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.id)
            .orderBy(OrderBy.of(PersonColumn.id, Order.DESC))
            .build();
        assertThat(query).matches("""
            SELECT id
            FROM person
            ORDER BY id DESC
            """);
        assertThat(query).containsNoArgs();
        assertEquals(IntArrayList.from(4, 3, 2, 1), SQL.runner().fetchIntColumn(query));
    }

    @Test
    public void runner_fetchLongColumn() {
        SelectQuery query = SelectWhere.from(PERSON_META)
            .select(PersonColumn.iq)
            .orderBy(OrderBy.of(PersonColumn.iq, Order.ASC))
            .build();
        assertThat(query).matches("""
            SELECT iq
            FROM person
            ORDER BY iq ASC
            """);
        assertThat(query).containsNoArgs();
        assertEquals(LongArrayList.from(100, 110, 120, 130), SQL.runner().fetchLongColumn(query));
    }

    private static @NotNull UnitSubject assertThat(@NotNull SelectQuery query) {
        return new UnitSubject((Unit) query);
    }
}
