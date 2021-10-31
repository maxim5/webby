package io.webby.util.sql;

import com.google.inject.Inject;
import io.webby.common.ClasspathScanner;
import io.webby.util.sql.schema.AdapterSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> dataClass) {
        String adapterName = AdapterSignature.defaultAdapterName(dataClass);
        return matchedClasses.get(adapterName);
    }

    public @NotNull FQN locateAdapterFqn(@NotNull Class<?> dataClass) {
        String adapterName = AdapterSignature.defaultAdapterName(dataClass);
        Class<?> klass = matchedClasses.get(adapterName);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(dataClass.getPackageName(), adapterName);
    }
}