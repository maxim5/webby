package io.webby.orm;

import com.google.inject.Inject;
import io.webby.app.ClassFilter;
import io.webby.app.Settings;
import io.webby.orm.codegen.DefaultModelAdaptersLocator;
import io.webby.util.classpath.ClasspathScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModelAdaptersScanner extends DefaultModelAdaptersLocator {
    @Inject
    public ModelAdaptersScanner(@NotNull Settings settings, @NotNull ClasspathScanner scanner) {
        super(scanAdapters(settings, scanner));
    }

    private static @NotNull Set<Class<?>> scanAdapters(@NotNull Settings settings, @NotNull ClasspathScanner scanner) {
        return scanner
            .timed(NAME)
            .scanToSet(ClassFilter.matchingAllOf(settings.modelFilter(), ClassFilter.of(FILTER_BY_NAME)));
    }
}
