package io.spbx.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.ScopedMock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.InlineMockMaker;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class Mocking {
    @CheckReturnValue
    @MustBeClosed
    public static @NotNull ScopedMock withMockedInstantNow(@NotNull Instant fixedInstant) {
        clearMock(Instant.class);
        MockedStatic<Instant> mock = mockStaticInstant(fixedInstant);
        return mock;
    }

    @CanIgnoreReturnValue
    public static @NotNull MockedStatic<Instant> mockInstantNowInline(@NotNull Instant fixedInstant) {
        clearMock(Instant.class);
        return mockStaticInstant(fixedInstant);
    }

    private static @NotNull MockedStatic<Instant> mockStaticInstant(@NotNull Instant fixedInstant) {
        MockedStatic<Instant> mocked = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS);
        mocked.when(Instant::now).thenReturn(fixedInstant);
        return mocked;
    }

    public static void clearMock(@NotNull Object mock) {
        if (Mockito.mockingDetails(mock).isMock()) {
            if (Plugins.getMockMaker() instanceof InlineMockMaker inlineMockMaker) {
                inlineMockMaker.clearMock(mock);
            } else {
                Mockito.clearAllCaches();  // last resort
            }
        }
    }

    public static Instant nowTruncatedToMillis() {
        Clock clock = Clock.fixed(Instant.now().truncatedTo(ChronoUnit.MILLIS), ZoneId.systemDefault());
        return Instant.now(clock);
    }
}
