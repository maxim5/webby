package io.webby.orm.arch.factory;

import io.webby.orm.adapter.lang.AtomicBooleanJdbcAdapter;
import io.webby.orm.adapter.lang.AtomicIntegerJdbcAdapter;
import io.webby.orm.adapter.lang.AtomicLongJdbcAdapter;
import io.webby.orm.arch.model.Column;
import io.webby.orm.arch.model.JdbcType;
import io.webby.orm.arch.model.PojoArch;
import io.webby.testing.orm.FakeModelAdaptersScanner;
import io.webby.util.base.EasyPrimitives.OptionalBool;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.webby.orm.arch.factory.TestingArch.assertThat;

public class RecursivePojoArchFactoryTest {
    private final FakeModelAdaptersScanner scanner = FakeModelAdaptersScanner.empty();
    private final RecursivePojoArchFactory factory = new RecursivePojoArchFactory(TestingArch.newRunContext(scanner));

    @Test
    public void pojo_native_columns() {
        record Foo(int x, String y) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("x", JdbcType.Int), Column.of("y", JdbcType.String));
    }

    @Test
    public void pojo_char_column() {
        record Foo(char ch) {}

        scanner.setupAdapters(FakeModelAdaptersScanner.DEFAULT_MAP);
        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("ch", JdbcType.String));
    }

    @Test
    public void pojo_wrapper_columns() {
        record Foo(Integer x, Long y) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("x", JdbcType.Int), Column.of("y", JdbcType.Long));
    }

    @Test
    public void pojo_enum_column() {
        record Foo(OptionalBool bool) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("bool", JdbcType.Int));
    }

    @Test
    public void pojo_standard_point_column() {
        record Foo(Point point) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("point_x", JdbcType.Int), Column.of("point_y", JdbcType.Int));
    }

    @Test
    public void pojo_atomic_columns() {
        record Foo(AtomicInteger x, AtomicLong y, AtomicBoolean z) {}

        scanner.setupAdapter(AtomicInteger.class, AtomicIntegerJdbcAdapter.class);
        scanner.setupAdapter(AtomicLong.class, AtomicLongJdbcAdapter.class);
        scanner.setupAdapter(AtomicBoolean.class, AtomicBooleanJdbcAdapter.class);
        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("x", JdbcType.Int), Column.of("y", JdbcType.Long), Column.of("z", JdbcType.Boolean));
    }

    @Test
    public void pojo_atomic_reference_column() {
        record Foo(AtomicReference<Integer> atomic) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("atomic", JdbcType.Int));
    }

    @Test
    public void pojo_optional_column() {
        record Foo(Optional<Integer> optional) {}

        PojoArch pojoArch = factory.buildPojoArchFor(Foo.class);
        assertThat(pojoArch)
            .hasAdapterName("FooJdbcAdapter")
            .hasColumns(Column.of("optional", JdbcType.Int));
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
