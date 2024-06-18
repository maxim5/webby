package io.spbx.webby.netty.intercept.attr;

import io.spbx.webby.app.AppConfigException;
import io.spbx.webby.netty.intercept.attr.AttributesValidator;
import io.spbx.webby.testing.ext.FluentLoggingCapture;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributesValidatorTest {
    @RegisterExtension static final FluentLoggingCapture LOGGING = new FluentLoggingCapture(AttributesValidator.class);

    @Test
    public void validatePositions_valid() {
        assertPositions(0, 0);
        assertPositions(1, 1, 0);
        assertPositions(1, 1);

        assertPositions(3, 1, 2, 3);
        assertPositions(3, 3, 2, 1);

        assertPositions(5, 0, 5);
        assertPositions(5, 1, 5, 3);
    }

    @Test
    public void validatePositions_invalid() {
        assertPositionsThrows(-1, 1);
        assertPositionsThrows(0, 0);
        assertPositionsThrows(1, 2, 3, 3);
    }

    @Test
    public void validatePositions_a_logged() {
        LOGGING.withCustomLog4jLevel(Level.ERROR, Level.DEBUG, () -> {
            assertPositionsLogged("Attribute positions between 0 and 2 are skipped", 0, 2);
            assertPositionsLogged("Attribute positions between 1 and 3 are skipped", 1, 3, 4);
            assertPositionsLogged("Attribute positions between 4 and 99 are skipped", 1, 2, 3, 4, 99);
        });
    }

    private static void assertPositions(int expected, int... positions) {
        assertThat(AttributesValidator.validatePositions(positions, value -> null)).isEqualTo(expected);
    }

    private static void assertPositionsThrows(int... positions) {
        assertThrows(AppConfigException.class, () -> AttributesValidator.validatePositions(positions, value -> null));
    }

    private static void assertPositionsLogged(String message, int... positions) {
        AttributesValidator.validatePositions(positions, value -> null);
        assertThat(LOGGING.logRecordsContaining(message)).isNotEmpty();
    }
}
