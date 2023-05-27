package io.webby.orm.arch.factory;

import io.webby.orm.arch.model.Column;
import io.webby.orm.arch.model.ColumnType;
import io.webby.orm.arch.model.JdbcType;
import io.webby.orm.arch.model.PojoArch;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class RecursivePojoArchFactoryTest {
    private final RecursivePojoArchFactory factory = new RecursivePojoArchFactory(TestingArch.newRunContext());

    @Test
    public void pojo_native_columns() {
        record Foo(int x, String y) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch.columns()).containsExactly(
            new Column("x", new ColumnType(JdbcType.Int)),
            new Column("y", new ColumnType(JdbcType.String))
        );
    }
}
