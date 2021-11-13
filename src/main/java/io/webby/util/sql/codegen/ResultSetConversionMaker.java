package io.webby.util.sql.codegen;

import io.webby.util.sql.api.Foreign;
import io.webby.util.sql.api.ForeignObj;
import io.webby.util.sql.api.ReadFollow;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static io.webby.util.sql.codegen.ColumnJoins.joinWithTransform;
import static java.util.Objects.requireNonNull;

class ResultSetConversionMaker {
    private static final Set<Class<?>> FULL_NAME_CLASSES = Set.of(java.util.Date.class, java.sql.Date.class);

    private final String resultSetParam;
    private final String followParam;
    private final String indexParam;

    public ResultSetConversionMaker(@NotNull String resultSetParam, @NotNull String followParam, @NotNull String indexParam) {
        this.resultSetParam = resultSetParam;
        this.followParam = followParam;
        this.indexParam = indexParam;
    }

    public @NotNull Snippet make(@NotNull TableSchema table) {
        return new Snippet().withLines(table.fields().stream().map(this::assignFieldLine));
    }

    private @NotNull String assignFieldLine(@NotNull TableField field) {
        Class<?> fieldType = field.javaType();
        String fieldClassName = FULL_NAME_CLASSES.contains(fieldType) ?
                fieldType.getName() :
                Naming.shortCanonicalName(fieldType);
        return "%s %s = %s;".formatted(fieldClassName, field.javaName(), fieldCreateExpr(field));
    }

    private @NotNull String fieldCreateExpr(@NotNull TableField field) {
        if (field.isForeignKey()) {
            assert field instanceof ForeignTableField : "Expected a foreign key field, but found %s".formatted(field);
            return foreignFieldSwitchExpr((ForeignTableField) field);
        } else if (field.isNativelySupportedType()) {
            assert field instanceof OneColumnTableField : "Native field is not one column: %s".formatted(field);
            return resultSetGetterExpr(((OneColumnTableField) field).column());
        } else {
            String staticRef = requireNonNull(field.adapterInfo()).staticRef();
            String params = joinWithTransform(field.columns(), this::resultSetGetterExpr);
            return "%s.createInstance(%s)".formatted(staticRef, params);
        }
    }

    private @NotNull String foreignFieldSwitchExpr(@NotNull ForeignTableField field) {
        Stream<String> cases = Arrays.stream(ReadFollow.values())
            .map(follow -> {
                String fkParam = resultSetGetterExpr(field.foreignKeyColumn());
                String params;
                String factoryMethod;
                if (follow == ReadFollow.NO_FOLLOW) {
                    params = fkParam;
                    factoryMethod = "ofId";
                } else {
                    params = "%s, %s.fromRow(%s, %s.%s, %s)".formatted(
                        fkParam,
                        field.getForeignTable().javaName(),
                        resultSetParam,
                        ReadFollow.class.getSimpleName(),
                        follow == ReadFollow.ONE_LEVEL ? ReadFollow.NO_FOLLOW : ReadFollow.FOLLOW_ALL,
                        indexParam
                    );
                    factoryMethod = "ofEntity";
                }
                Class<?> factoryClass = field.javaType() == Foreign.class ? ForeignObj.class : field.javaType();
                return "case %s -> %s.%s(%s);".formatted(follow, factoryClass.getSimpleName(), factoryMethod, params);
            });
        return new Snippet()
                .withFormattedLine("switch (%s) {", followParam)
                .withLines(cases.map(line -> BaseCodegen.INDENT + line))
                .withLine("}").join(BaseCodegen.INDENT);
    }

    private @NotNull String resultSetGetterExpr(@NotNull Column column) {
        String getter = column.type().jdbcType().getterMethod();
        return "result.%s(++%s)".formatted(getter, indexParam);
    }
}
