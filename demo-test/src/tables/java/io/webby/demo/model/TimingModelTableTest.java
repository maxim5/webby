package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;

import static io.webby.testing.TestingUtil.array;

public class TimingModelTableTest
        extends SqlDbTableTest<Timestamp, TimingModel, TimingModelTable>
        implements PrimaryKeyTableTest<Timestamp, TimingModel, TimingModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new TimingModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull Timestamp[] keys() {
        return array(Timestamp.valueOf("2000-01-01 00:00:00"),
                     Timestamp.valueOf("2010-01-01 00:00:00"),
                     Timestamp.valueOf("2020-01-01 00:00:00"));
    }

    @Override
    public @NotNull TimingModel createEntity(@NotNull Timestamp key, int version) {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.MILLIS).minus(version, ChronoUnit.MINUTES);
        LocalDate localDate = LocalDate.ofInstant(instant.truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault());
        LocalTime localTime = LocalTime.ofInstant(instant.truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault());
        ZoneOffset zoneOffset = ZoneOffset.UTC;
        return new TimingModel(
            key,
            connector().engine() == Engine.SQLite ? java.util.Date.from(instant) : java.sql.Date.valueOf(localDate),
            java.sql.Date.valueOf(localDate),
            Time.valueOf(localTime),
            instant,
            Timestamp.from(instant),
            localDate,
            localTime,
            LocalDateTime.of(localDate, localTime),
            ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault()),
            OffsetTime.of(localTime, zoneOffset),
            OffsetDateTime.of(localDate, localTime, zoneOffset),
            Duration.ofDays(30),
            Period.of(1, 2, 3),
            zoneOffset
        );
    }
}
