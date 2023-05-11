package io.webby.netty.intercept;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public interface InterceptorsStack {
    @NotNull ImmutableList<InterceptItem> stack();

    default @NotNull Optional<Interceptor> findEnabledInterceptor(@NotNull Predicate<Interceptor> predicate) {
        return stack().stream().map(InterceptItem::instance).filter(predicate).findFirst();
    }
}
