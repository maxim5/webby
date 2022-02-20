package io.webby.orm.arch;

import io.webby.orm.adapter.time.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class AdapterApiTest {
    @Test
    public void classToAdapterColumns_duration_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(DurationJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(new Column("foo", new ColumnType(JdbcType.Long))).inOrder();
    }

    @Test
    public void classToAdapterColumns_instant_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(InstantJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(new Column("foo", new ColumnType(JdbcType.Timestamp))).inOrder();
    }

    @Test
    public void classToAdapterColumns_offset_time_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(OffsetTimeJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(new Column("foo_time", new ColumnType(JdbcType.Time)),
                                            new Column("foo_zone_offset_seconds", new ColumnType(JdbcType.Int))).inOrder();
    }

    @Test
    public void classToAdapterColumns_offset_date_time_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(OffsetDateTimeJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(new Column("foo_timestamp", new ColumnType(JdbcType.Timestamp)),
                                            new Column("foo_zone_offset_seconds", new ColumnType(JdbcType.Int))).inOrder();
    }

    @Test
    public void classToAdapterColumns_period_adapter() {
        List<Column> columns = AdapterApi.classToAdapterColumns(PeriodJdbcAdapter.class, "foo");
        assertThat(columns).containsExactly(new Column("foo", new ColumnType(JdbcType.String))).inOrder();
    }
}
