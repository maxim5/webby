package io.spbx.webby.testing;

import io.spbx.orm.api.BaseTable;
import org.jetbrains.annotations.NotNull;

public interface TableTestApi<E, T extends BaseTable<E>> {
    @NotNull T table();
}
