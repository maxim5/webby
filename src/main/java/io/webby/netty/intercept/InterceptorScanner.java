package io.webby.netty.intercept;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.AppConfigException;
import io.webby.common.ClasspathScanner;
import io.webby.netty.intercept.attr.AttributeOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.logging.Level;

public class InterceptorScanner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final BiPredicate<String, String> filter = (packageName, className) -> packageName.startsWith("io.webby");  // TODO: hard-coded

    @Inject private ClasspathScanner scanner;

    @NotNull
    public Set<? extends Class<?>> getInterceptors() {
        return scanner.getDerivedClasses(filter, Interceptor.class);
    }

    @NotNull
    public Set<? extends Class<?>> getAttributeOwners() {
        return scanner.getAnnotatedClasses(filter, AttributeOwner.class);
    }

    private static int validateAttributeOwners(@NotNull Set<? extends Class<?>> classes) {
        int[] positions = classes.stream()
                .mapToInt(klass -> klass.getAnnotation(AttributeOwner.class).position())
                .toArray();
        return validatePositions(
                positions,
                pos -> classes.stream()
                        .filter(klass -> klass.getAnnotation(AttributeOwner.class).position() == pos)
                        .toList()
        );
    }

    @VisibleForTesting
    static int validatePositions(int[] positions, @NotNull IntFunction<List<? extends Class<?>>> lookup) {
        Arrays.sort(positions);
        return Arrays.stream(positions).reduce((x, y) -> {
            if (x < 0) {
                throw new AppConfigException("Attribute position can't be negative: %s".formatted(lookup.apply(x)));
            }
            // No need to check y, since x <= y
            if (x == y) {
                throw new AppConfigException("Attribute position is duplicated: %s".formatted(lookup.apply(x)));
            }
            if (x + 1 != y) {
                log.at(Level.FINE).log("Attribute positions between %d and %d are skipped", x, y);
            }
            return y;
        }).orElse(-1);
    }

    /*
    // TODO: intellij bug
    public static void main(String[] args) {
        int max = IntStream.of(1, 2, 3, 3).reduce((x, y) -> {
            if (x == y) {
                throw new RuntimeException();
            }
            return y;
        }).orElse(-1);
        System.out.println(max);
    }
    */
}
