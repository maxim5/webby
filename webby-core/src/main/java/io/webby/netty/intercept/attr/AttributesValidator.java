package io.webby.netty.intercept.attr;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.netty.intercept.InterceptItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;
import java.util.logging.Level;

import static io.webby.app.AppConfigException.failIf;

public class AttributesValidator {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_POSITION = 255;

    @CanIgnoreReturnValue
    public static int validateAttributeOwners(@NotNull Collection<InterceptItem> items) {
        int[] positions = items.stream()
            .filter(InterceptItem::isOwner)
            .mapToInt(InterceptItem::position)
            .toArray();
        return validatePositions(
            positions,
            pos -> items.stream().filter(item -> item.position() == pos).map(InterceptItem::instance).toList()
        );
    }

    @VisibleForTesting
    static int validatePositions(int @NotNull [] positions, @NotNull IntFunction<List<?>> lookup) {
        Arrays.sort(positions);
        return Arrays.stream(positions).reduce((x, y) -> {
            failIf(x < 0, "Attribute position can't be negative: %s", lookup.apply(x));
            // No need to check y, since x <= y
            failIf(y > MAX_POSITION, "Attribute position can't exceed %d: %s", MAX_POSITION, lookup.apply(y));
            // No need to check x, since x <= y
            failIf(x == y, "Attribute position is duplicated: %s", lookup.apply(x));
            if (x + 1 != y) {
                log.at(Level.FINE).log("Attribute positions between %d and %d are skipped", x, y);
            }
            return y;
        }).orElse(-1);
    }
}
