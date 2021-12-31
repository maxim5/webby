package io.webby.app;

import org.jetbrains.annotations.NotNull;

public interface SettingsFilters {
    @NotNull ClassFilter modelFilter();

    @NotNull ClassFilter handlerFilter();

    @NotNull ClassFilter interceptorFilter();
}
