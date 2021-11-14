package io.webby.util.sql.codegen;

import io.webby.util.sql.schema.TableSchema;
import org.jetbrains.annotations.NotNull;

class DeleteMaker {
    public static @NotNull Snippet make(@NotNull TableSchema table) {
         return new Snippet().withLine("DELETE FROM ", table.sqlName());
    }
}
