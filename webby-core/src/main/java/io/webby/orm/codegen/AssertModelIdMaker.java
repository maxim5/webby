package io.webby.orm.codegen;

import io.webby.orm.arch.TableArch;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class AssertModelIdMaker {
    public static @NotNull Snippet makeAssert(@NotNull String param, @NotNull TableArch table) {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return new Snippet().withLine("// No MySQL assert since PK is not integer");
        }
        String pattern = """
            assert !(%s.%s() == 0 && engine() == Engine.MySQL) :
                    "Null PK is treated as an auto-increment in MySQL. Call insertAutoIncPk() instead";""";
        return new Snippet().withFormattedLine(pattern, param, requireNonNull(table.primaryKeyField()).javaGetter());
    }
}