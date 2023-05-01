package io.webby.orm.codegen;

import io.webby.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class AssertModelIdMaker {
    public static @NotNull Snippet makeAssert(@NotNull String param, @NotNull TableArch table) {
        if (!table.isPrimaryKeyInt() && !table.isPrimaryKeyLong()) {
            return new Snippet().withLine(JavaSupport.EMPTY_LINE);
        }
        String pattern = """
            assert !(%s.%s == 0 && engine() == Engine.MySQL) :
                    "Null PK is treated as an auto-increment in MySQL. Call insertAutoIncPk() instead. Value: " + %s;""";
        return new Snippet().withFormattedLine(pattern, param, requireNonNull(table.primaryKeyField()).javaAccessor(), param);
    }
}
