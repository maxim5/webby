package io.webby.netty.intercept.attr;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppConfigException;
import io.webby.netty.intercept.InterceptItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;
import java.util.logging.Level;

public class AttributesValidator {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_POSITION = 255;

    @NotNull
    public static Result validateAttributeOwners(@NotNull Collection<InterceptItem> items) {
        int[] positions = items.stream()
                .filter(InterceptItem::isOwner)
                .mapToInt(InterceptItem::position)
                .toArray();
        int maxPosition = validatePositions(
                positions,
                pos -> items.stream()
                        .filter(item -> item.position() == pos)
                        .map(InterceptItem::instance)
                        .toList()
        );
        return new Result(maxPosition);
    }

    @VisibleForTesting
    static int validatePositions(int[] positions, @NotNull IntFunction<List<?>> lookup) {
        Arrays.sort(positions);
        return Arrays.stream(positions).reduce((x, y) -> {
            if (x < 0) {
                throw new AppConfigException("Attribute position can't be negative: %s".formatted(lookup.apply(x)));
            }
            // No need to check y, since x <= y
            if (y > MAX_POSITION) {
                throw new AppConfigException("Attribute position can't exceed %d: %s".formatted(MAX_POSITION, lookup.apply(y)));
            }
            // No need to check x, since x <= y
            if (x == y) {
                throw new AppConfigException("Attribute position is duplicated: %s".formatted(lookup.apply(x)));
            }
            if (x + 1 != y) {
                log.at(Level.FINE).log("Attribute positions between %d and %d are skipped", x, y);
            }
            return y;
        }).orElse(-1);
    }

    public record Result(int maxPosition) {}
}
