package io.webby.testing;

import io.webby.orm.api.BaseTable;
import org.jetbrains.annotations.NotNull;

public interface TableTestApi<E, T extends BaseTable<E>> {
    @NotNull T table();
}
