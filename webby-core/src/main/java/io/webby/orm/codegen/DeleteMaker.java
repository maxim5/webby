package io.webby.orm.codegen;

import io.webby.orm.arch.field.TableArch;
import org.jetbrains.annotations.NotNull;

class DeleteMaker {
    public static @NotNull Snippet make(@NotNull TableArch table) {
         return new Snippet().withLine("DELETE FROM ", table.sqlName());
    }
}
