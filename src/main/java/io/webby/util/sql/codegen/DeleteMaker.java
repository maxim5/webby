package io.webby.util.sql.codegen;

import io.webby.util.sql.arch.TableArch;
import org.jetbrains.annotations.NotNull;

class DeleteMaker {
    public static @NotNull Snippet make(@NotNull TableArch table) {
         return new Snippet().withLine("DELETE FROM ", table.sqlName());
    }
}
