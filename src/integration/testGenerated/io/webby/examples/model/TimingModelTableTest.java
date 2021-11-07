package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;

import static io.webby.testing.TestingUtil.array;

public class TimingModelTableTest extends BaseModelKeyTableTest<Timestamp, TimingModel, TimingModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE timing_model (
                id INTEGER PRIMARY KEY,
                util_date INTEGER,
                sql_date INTEGER,
                time INTEGER,
                instant INTEGER,
                timestamp INTEGER,
                local_date INTEGER,
                local_time INTEGER,
                local_date_time INTEGER,
                zoned_date_time INTEGER,
                offset_time_arg0 INTEGER,
                offset_time_arg1 INTEGER,
                offset_date_time_arg0 INTEGER,
                offset_date_time_arg1 INTEGER,
                duration INTEGER,
                period TEXT,
                zone_offset INTEGER
            )
        """);
        // TODO: fix sql names
        keys = array(Timestamp.valueOf("2000-01-01 00:00:00"), Timestamp.valueOf("2020-01-01 00:00:00"));
        table = new TimingModelTable(connection);
    }

    @Override
    protected @NotNull TimingModel createEntity(@NotNull Timestamp key, int version) {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.MILLIS).minus(version, ChronoUnit.MINUTES);
        LocalDate localDate = LocalDate.ofInstant(instant.truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault());
        LocalTime localTime = LocalTime.ofInstant(instant.truncatedTo(ChronoUnit.SECONDS), ZoneId.systemDefault());
        ZoneOffset zoneOffset = ZoneOffset.UTC;
        return new TimingModel(key,
                               java.util.Date.from(instant),
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
                               zoneOffset);
    }
}
