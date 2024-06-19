package io.spbx.util.time;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.util.testing.TestingBasics;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings({ "SameParameterValue", "RedundantThrows", "CodeBlock2Expr" })
public class TimeItTest {
    private static final long WAIT_MILLIS = 5;

    @Test
    public void timeIt_runnable() {
        TimeIt.timeIt(() -> {
            action(WAIT_MILLIS);
        }).onDone(millis -> {
            assertThat(millis).isAtLeast(WAIT_MILLIS - 1);
        });
    }

    @Test
    public void timeIt_runnable_throws() throws Exception {
        TimeIt.timeIt(() -> {
            throwingAction(WAIT_MILLIS);
        }).onDone(millis -> {
            assertThat(millis).isAtLeast(WAIT_MILLIS - 1);
        });
    }

    @Test
    public void timeIt_supplier() {
        TimeIt.timeIt(() -> {
            return action(WAIT_MILLIS);
        }).onDone((value, millis) -> {
            assertThat(value).isEqualTo(WAIT_MILLIS);
            assertThat(millis).isAtLeast(WAIT_MILLIS - 1);
        });
    }

    @Test
    public void timeIt_supplier_throws() throws Exception {
        TimeIt.timeIt(() -> {
            return throwingAction(WAIT_MILLIS);
        }).onDone((value, millis) -> {
            assertThat(value).isEqualTo(WAIT_MILLIS);
            assertThat(millis).isAtLeast(WAIT_MILLIS - 1);
        });
    }

    @CanIgnoreReturnValue
    private static long action(long waitMillis) {
        TestingBasics.waitFor(waitMillis);
        return waitMillis;
    }

    @CanIgnoreReturnValue
    private static long throwingAction(long waitMillis) throws Exception {
        TestingBasics.waitFor(waitMillis);
        return waitMillis;
    }
}
