package io.webby.orm.codegen;


import io.webby.orm.arch.model.TableArch;
import io.webby.orm.codegen.InsertMaker.Ignore;
import org.junit.jupiter.api.Test;

import static io.webby.orm.arch.factory.TestingArch.buildTableArch;
import static io.webby.orm.codegen.AssertSnippet.assertThatSql;

public class InsertMakerTest {
    @Test
    public void make_one_column() {
        record User(boolean value) {}

        TableArch tableArch = buildTableArch(User.class);

        assertThatSql(new InsertMaker(Ignore.DEFAULT).makeAll(tableArch)).matches("""
            INSERT INTO user (value)
            VALUES (?)
            """);
        assertThatSql(new InsertMaker(Ignore.IGNORE).makeAll(tableArch)).matches("""
            INSERT IGNORE INTO user (value)
            VALUES (?)
            """);
        assertThatSql(new InsertMaker(Ignore.OR_IGNORE).makeAll(tableArch)).matches("""
            INSERT OR IGNORE INTO user (value)
            VALUES (?)
            """);
    }

    @Test
    public void make_two_columns() {
        record User(int userId, String name) {}

        TableArch tableArch = buildTableArch(User.class);

        assertThatSql(new InsertMaker(Ignore.DEFAULT).makeAll(tableArch)).matches("""
            INSERT INTO user (user_id, name)
            VALUES (?, ?)
            """);
        assertThatSql(new InsertMaker(Ignore.IGNORE).makeAll(tableArch)).matches("""
            INSERT IGNORE INTO user (user_id, name)
            VALUES (?, ?)
            """);
        assertThatSql(new InsertMaker(Ignore.OR_IGNORE).makeAll(tableArch)).matches("""
            INSERT OR IGNORE INTO user (user_id, name)
            VALUES (?, ?)
            """);
    }
}
