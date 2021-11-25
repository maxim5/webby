package io.webby.db.sql.testing;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.Term;
import io.webby.orm.api.query.TermType;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.webby.util.base.EasyCast.castAny;

// Doesn't work as expected at the moment.
// Meta should include: type (jdbc?) and is-primary?
public class SqlSchemaMaker {
    @SuppressWarnings("unchecked")
    public static @NotNull String makeCreateTableQuery(@NotNull Class<? extends BaseTable<?>> tableClass) {
        try {
            TableMeta meta = castAny(tableClass.getField("META").get(null));

            Class<Enum<?>> enumClass = Arrays.stream(tableClass.getDeclaredClasses())
                    .filter(klass -> klass.getSimpleName().equals("OwnColumn"))
                    .map(klass -> (Class<Enum<?>>) klass)
                    .findFirst()
                    .orElseThrow();
            Map<String, TermType> termTypes = Arrays.stream(enumClass.getEnumConstants())
                    .map(constant -> (Column) constant)
                    .collect(Collectors.toMap(Column::name, Term::type));

            String tableName = meta.sqlTableName();
            List<String> columns = meta.sqlColumns();
            String definitions = columns.stream().map(column -> {
                String sqlType = switch (termTypes.get(column)) {
                    case STRING -> "TEXT";
                    case NUMBER, BOOL, TIME -> "INTEGER";
                    case WILDCARD -> throw new IllegalStateException("Invalid column type: %s".formatted(column));
                };
                return "%s %s".formatted(column, sqlType);
            }).collect(Collectors.joining(",\n    "));

            return """
            CREATE TABLE IF NOT EXISTS %s (
                %s
            )
            """.formatted(tableName, definitions);
        } catch (Throwable e) {
            return Rethrow.rethrow(e);
        }
    }

    public static @NotNull String makeCreateTableQuery(@NotNull Class<? extends BaseTable<?>> ... tableClasses) {
        return Arrays.stream(tableClasses).map(SqlSchemaMaker::makeCreateTableQuery).collect(Collectors.joining(";\n"));
    }
}
