package io.webby.orm.api.query;

import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.api.query.CompareType.EQ;
import static io.webby.orm.api.query.PersonTableData.*;
import static io.webby.orm.api.query.Shortcuts.literal;
import static io.webby.testing.TestingUtil.array;

@Tag("sql")
public class SqlDbQueryTest {
    @RegisterExtension private static final SqlDbSetupExtension SQL_DB = SqlDbSetupExtension.fromProperties();

    @BeforeAll
    static void beforeAll() {
        SQL_DB.runUpdate("DROP TABLE IF EXISTS person");
        SQL_DB.runUpdate(SqlSchemaMaker.makeCreateTableQuery(SQL_DB.engine(), PERSON_META));

        List<Object[]> rows = List.of(
            array("Kate", "DE", F, parseDate("1990-01-01"), 110, 160.5, photo(0x1111111)),
            array("Bill", "US", M, parseDate("1971-10-20"), 120, 170.1, photo(0x2222222)),
            array("Ivan", "RU", M, parseDate("1999-07-16"), 100, 175.9, photo(0x3333333)),
            array("Yuan", "CN", F, parseDate("2005-05-31"), 130, 156.2, photo(0x4444444))
        );
        for (Object[] row : rows) {
            SQL_DB.runUpdate("""
                INSERT INTO person (name, country, sex, birthday, iq, height, photo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """, row);
        }
    }

    @Test
    public void simple() {
        assertThat(
            SQL_DB.runQueryToString(
                "SELECT id FROM person " +
                Where.of(EQ.compare(PersonColumn.name, literal("Bill")))
            )
        ).isEqualTo("""
            ------
            | id |
            ------
            | 2  |
            ------\
            """);
    }
}
