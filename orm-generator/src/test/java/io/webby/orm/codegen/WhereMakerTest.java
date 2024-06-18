package io.webby.orm.codegen;

import io.spbx.orm.api.annotate.Sql;
import io.webby.orm.arch.model.TableArch;
import org.junit.jupiter.api.Test;

import static io.webby.orm.arch.factory.TestingArch.buildTableArch;
import static io.webby.orm.codegen.AssertSnippet.assertThatSql;

public class WhereMakerTest {
    @Test
    public void make_one_pk_column() {
        record User(int userId, String name) {}

        TableArch tableArch = buildTableArch(User.class);
        assertThatSql(WhereMaker.makeForPrimaryColumns(tableArch)).isEqualTo("WHERE user.user_id=?");
    }

    @Test
    public void make_two_pk_columns() {
        record User(@Sql.PK int id1, @Sql.PK int id2) {}

        TableArch tableArch = buildTableArch(User.class);
        assertThatSql(WhereMaker.makeForPrimaryColumns(tableArch)).isEqualTo("WHERE user.id1=? AND user.id2=?");
    }
}
