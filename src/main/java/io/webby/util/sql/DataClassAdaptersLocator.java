package io.webby.util.sql;

import com.google.inject.Inject;
import io.webby.common.ClasspathScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataClassAdaptersLocator {
    private static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");

    private final Map<String, Class<?>> matchedClasses;

    @Inject
    public DataClassAdaptersLocator(@NotNull ClasspathScanner scanner) {
        matchedClasses = scanner.getMatchingClasses(
            (pkg, klass) -> ADAPTER_NAME.matcher(klass).matches(),
            klass -> true,
            "data adapter"
        ).stream().collect(Collectors.toMap(Class::getSimpleName, klass -> klass));
    }

    public @NotNull FQN locate(@NotNull Class<?> dataClass) {
        String adapterName = adapterName(dataClass);
        Class<?> klass = matchedClasses.get(adapterName);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(dataClass.getPackageName(), adapterName);
    }

    public static @NotNull String adapterName(@NotNull Class<?> klass) {
        return "%sJdbcAdapter".formatted(klass.getSimpleName());
    }
}
