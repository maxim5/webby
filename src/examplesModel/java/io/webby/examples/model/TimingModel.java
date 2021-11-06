package io.webby.examples.model;

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
                          ZonedDateTime zonedDateTime
                          /*DateTime googleDateTime*/) {
}
