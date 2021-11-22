package io.webby.orm.codegen;

import io.webby.util.func.ObjIntBiFunction;
import io.webby.orm.arch.TableField;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

class ValuesArrayMaker {
    private final String param;
    private final List<FieldConverter> converters;

    public ValuesArrayMaker(@NotNull String param, @NotNull Iterable<TableField> fields) {
        this.param = param;
        this.converters = zipWithColumnIndex(fields, FieldConverter::new);
    }

    public @NotNull Snippet makeInitValues() {
        return new Snippet().withLines(converters.stream().flatMap(FieldConverter::initLines));
    }

    public @NotNull Snippet makeConvertValues() {
        return new Snippet().withLines(converters.stream().map(FieldConverter::fillValuesLine));
    }

    private static <T> @NotNull List<T> zipWithColumnIndex(@NotNull Iterable<TableField> fields,
                                                           @NotNull ObjIntBiFunction<TableField, T> converter) {
        ArrayList<T> result = new ArrayList<>();
        int columnIndex = 0;
        for (TableField field : fields) {
            result.add(converter.apply(field, columnIndex));
            columnIndex += field.columnsNumber();
        }
        return result;
    }

    private class FieldConverter {
        private final TableField field;
        private final int columnIndex;

        private FieldConverter(@NotNull TableField field, int columnIndex) {
            this.field = field;
            this.columnIndex = columnIndex;
        }

        public @NotNull Stream<String> initLines() {
            if (field.isNativelySupportedType()) {
                return Stream.of("%s.%s(),".formatted(param, field.javaGetter()));
            }
            if (field.isForeignKey()) {
                return Stream.of("%s.%s().getFk(),".formatted(param, field.javaGetter()));
            }
            return Stream.generate(() -> "null,").limit(field.columnsNumber());
        }

        public @NotNull String fillValuesLine() {
            if (field.isNativelySupportedType()) {
                return "";
            }
            if (field.isForeignKey()) {
                return "";
            }
            return "%s.fillArrayValues(%s.%s(), array, %d);"
                    .formatted(requireNonNull(field.adapterApi()).staticRef(), param, field.javaGetter(), columnIndex);
        }
    }
}
