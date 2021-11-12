package io.webby.util.sql.codegen;

import io.webby.util.sql.schema.TableField;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

record FieldArrayConversionMaker(@NotNull TableField field, int columnIndex) {
    public @NotNull Stream<String> initLines() {
        if (!field.isNativelySupportedType()) {
            return Stream.generate(() -> "null,").limit(field.columnsNumber());
        }
        return Stream.of("$model_param.%s(),".formatted(field.javaGetter()));
    }

    public @NotNull String fillValuesLine() {
        if (field.isNativelySupportedType()) {
            return "";
        }
        return "%s.fillArrayValues($model_param.%s(), array, %d);"
                .formatted(requireNonNull(field.adapterInfo()).staticRef(), field.javaGetter(), columnIndex);
    }
}
