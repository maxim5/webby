package io.spbx.webby.orm;

import com.google.inject.Inject;
import io.spbx.orm.codegen.DefaultModelAdaptersLocator;
import io.spbx.util.classpath.ClasspathScanner;
import io.spbx.webby.app.ClassFilter;
import io.spbx.webby.app.Settings;
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
