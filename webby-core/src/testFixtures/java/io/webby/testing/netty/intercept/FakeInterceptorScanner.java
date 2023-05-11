package io.webby.testing.netty.intercept;

import com.google.inject.Module;
import io.webby.auth.AuthInterceptor;
import io.webby.auth.session.SessionInterceptor;
import io.webby.netty.intercept.InterceptItem;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.InterceptorScanner;
import io.webby.perf.stats.impl.StatsInterceptor;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FakeInterceptorScanner extends InterceptorScanner {
    public static final FakeInterceptorScanner FAKE_SCANNER = new FakeInterceptorScanner();
    public static final FakeInterceptorScanner DEFAULT_SCANNER = of(
        new StatsInterceptor(),
        new SessionInterceptor(),
        new AuthInterceptor()
    );

    private final List<InterceptItem> interceptors;

    public FakeInterceptorScanner() {
        this(new ArrayList<>());
    }

    public FakeInterceptorScanner(@NotNull List<InterceptItem> interceptors) {
        this.interceptors = interceptors;
    }

    public static @NotNull FakeInterceptorScanner of(@NotNull InterceptItem @NotNull... items) {
        return new FakeInterceptorScanner(List.of(items));
    }

    public static @NotNull FakeInterceptorScanner of(@NotNull Interceptor @NotNull... interceptors) {
        List<InterceptItem> items = Stream.of(interceptors)
            .map(interceptor -> toItem(interceptor, interceptor.getClass()))
            .sorted(Comparator.comparingInt(InterceptItem::position))
            .toList();
        return new FakeInterceptorScanner(items);
    }

    @Override
    public @NotNull List<InterceptItem> getInterceptorsFromClasspath() {
        return interceptors;
    }

    public @NotNull Module asGuiceModule() {
        return TestingModules.instance(InterceptorScanner.class, this);
    }
}
