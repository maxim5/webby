package io.webby.orm.arch.factory;

import io.webby.orm.arch.model.Column;
import io.webby.orm.arch.model.JdbcType;
import io.webby.orm.arch.model.PojoArch;
import org.junit.jupiter.api.Test;

import static io.webby.orm.arch.factory.TestingArch.assertThat;

public class RecursivePojoArchFactoryTest {
    private final RecursivePojoArchFactory factory = new RecursivePojoArchFactory(TestingArch.newRunContext());

    @Test
    public void pojo_native_columns() {
        record Foo(int x, String y) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("x", JdbcType.Int), Column.of("y", JdbcType.String));
    }

    @Test
    public void pojo_nested_level_one() {
        record Foo(int x, long y) {}
        record Bar(boolean z, Foo foo) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Bar.class);
        assertThat(pojoArch)
            .hasAdapterName("BarJdbcAdapter")
            .hasColumns(Column.of("z", JdbcType.Boolean), Column.of("foo_x", JdbcType.Int), Column.of("foo_y", JdbcType.Long));
    }
}
