package io.webby.demo.model;

import java.sql.Timestamp;
import java.time.*;

public record TimingModel(Timestamp id,
                          java.util.Date utilDate,
                          java.sql.Date sqlDate,
                          java.sql.Time time,
                          Instant instant,
                          Timestamp timestamp,
                          LocalDate localDate,
                          LocalTime localTime,
                          LocalDateTime localDateTime,
                          ZonedDateTime zonedDateTime,
                          OffsetTime offsetTime,
                          OffsetDateTime offsetDateTime,
                          Duration duration,
                          Period period,
                          ZoneOffset zoneOffset
                          /*DateTime googleDateTime*/) {
}
