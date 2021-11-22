package io.webby.orm.arch;

import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;

public class Naming {
    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelLowerToUpper(@NotNull String s) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL).convert(s);
    }

    public static @NotNull String generatedSimpleName(@NotNull Class<?> klass) {
        if (klass.isMemberClass()) {
            return shortCanonicalName(klass).replace('.', '_');
        }
        return klass.getSimpleName();
    }

    public static @NotNull String shortCanonicalName(@NotNull Class<?> klass) {
        if (klass.isMemberClass()) {
            return cutPrefix(klass.getCanonicalName(), klass.getPackageName(), + 1);
        }
        return klass.getSimpleName();
    }

    public static @NotNull String cutPrefix(@NotNull String big, @NotNull String small) {
        return cutPrefix(big, small, 0);
    }

    public static @NotNull String cutPrefix(@NotNull String big, @NotNull String small, int extraCut) {
        return big.substring(small.length() + extraCut);
    }

    public static @NotNull String cutSuffix(@NotNull String big, @NotNull String small) {
        return cutSuffix(big, small, 0);
    }

    public static @NotNull String cutSuffix(@NotNull String big, @NotNull String small, int extraCut) {
        return big.substring(0, big.length() - small.length() - extraCut);
    }
}
