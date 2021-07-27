package io.webby.netty.intercept.attr;

import io.webby.app.AppConfigException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AttributesValidatorTest {
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

    private void assertPositions(int expected, int... positions) {
        Assertions.assertEquals(expected, AttributesValidator.validatePositions(positions, value -> null));
    }

    private void assertPositionsThrows(int... positions) {
        Assertions.assertThrows(AppConfigException.class, () -> AttributesValidator.validatePositions(positions, value -> null));
    }
}
