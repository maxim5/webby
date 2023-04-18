package io.webby.orm.codegen;

import io.webby.orm.api.Foreign;
import io.webby.orm.api.ForeignObj;
import io.webby.orm.api.ReadFollow;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.model.ForeignTableField;
import io.webby.orm.arch.model.OneColumnTableField;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.arch.model.TableField;
import io.webby.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static io.webby.orm.api.ReadFollow.*;
import static io.webby.orm.codegen.JavaSupport.INDENT1;
import static io.webby.orm.codegen.Joining.COMMA_JOINER;

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

    public @NotNull Snippet make(@NotNull TableArch table) {
        return new Snippet().withLines(table.fields().stream().map(this::assignFieldLine));
    }

    private @NotNull String assignFieldLine(@NotNull TableField field) {
        Class<?> fieldType = field.javaType();
        String fieldClassName = FULL_NAME_CLASSES.contains(fieldType) ?
                fieldType.getName() :
                Naming.shortCanonicalJavaName(fieldType);
        return "%s %s = %s;".formatted(fieldClassName, field.javaName(), fieldCreateExpr(field));
    }

    private @NotNull String fieldCreateExpr(@NotNull TableField field) {
        return switch (field.typeSupport()) {
            case NATIVE -> {
                assert field instanceof OneColumnTableField : "Native field is not one column: %s".formatted(field);
                yield resultSetGetterExpr(((OneColumnTableField) field).column());
            }
            case FOREIGN_KEY -> {
                assert field instanceof ForeignTableField : "Expected a foreign key field, but found %s".formatted(field);
                yield foreignFieldSwitchExpr((ForeignTableField) field);
            }
            case MAPPER_API -> {
                assert field instanceof OneColumnTableField : "Mapped field is not one column: %s".formatted(field);
                String resultSetGetterExpr = resultSetGetterExpr(((OneColumnTableField) field).column());
                yield field.mapperApiOrDie().expr().jdbcToField(resultSetGetterExpr);
            }
            case ADAPTER_API -> {
                String params = field.columns().stream().map(this::resultSetGetterExpr).collect(COMMA_JOINER);
                yield field.adapterApiOrDie().expr().createInstance(params);
            }
        };
    }

    private @NotNull String foreignFieldSwitchExpr(@NotNull ForeignTableField field) {
        Stream<String> cases = Arrays.stream(ReadFollow.values())
            .map(follow -> {
                String fkParam = resultSetGetterExpr(field.foreignKeyColumn());
                String params;
                String factoryMethod;
                if (follow == NO_FOLLOW) {
                    params = fkParam;
                    factoryMethod = "ofId";
                } else {
                    int columnsNumber = field.columnsNumber(follow) - 1;  // exclude FK columns
                    params = "%s, %s.fromRow(%s, %s.%s, (%s += %d) - %d)".formatted(
                        fkParam,
                        field.getForeignTable().javaName(),
                        resultSetParam,
                        ReadFollow.class.getSimpleName(),
                        follow == FOLLOW_ONE_LEVEL ? NO_FOLLOW : FOLLOW_ALL,
                        indexParam, columnsNumber, columnsNumber
                    );
                    factoryMethod = "ofEntity";
                }
                Class<?> factoryClass = field.javaType() == Foreign.class ? ForeignObj.class : field.javaType();
                return "case %s -> %s.%s(%s);".formatted(follow, factoryClass.getSimpleName(), factoryMethod, params);
            });
        return new Snippet()
                .withFormattedLine("switch (%s) {", followParam)
                .withLines(cases.map(line -> INDENT1 + line))
                .withLine("}").join(INDENT1);
    }

    private @NotNull String resultSetGetterExpr(@NotNull Column column) {
        String getter = column.type().jdbcType().getterMethod();
        return "result.%s(++%s)".formatted(getter, indexParam);
    }
}
