package io.webby.orm.codegen;

import com.google.inject.Inject;
import io.webby.app.ClassFilter;
import io.webby.app.Settings;
import io.webby.util.classpath.ClasspathScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AppModelAdaptersScanner extends DefaultModelAdaptersLocator {
    @Inject
    public AppModelAdaptersScanner(@NotNull Settings settings, @NotNull ClasspathScanner scanner) {
        super(scanAdapters(settings, scanner));
    }

    private static @NotNull Set<Class<?>> scanAdapters(@NotNull Settings settings, @NotNull ClasspathScanner scanner) {
        return scanner
            .timed(NAME)
            .scanToSet(ClassFilter.matchingAllOf(settings.modelFilter(), ClassFilter.of(FILTER_BY_NAME)));
    }
}
