package io.webby.orm.codegen;

import com.google.common.collect.Streams;
import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.adapter.JdbcArrayAdapter;
import io.webby.orm.adapter.JdbcSingleValueAdapter;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.Naming;
import io.webby.orm.arch.model.*;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.webby.orm.codegen.JavaSupport.INDENT1;
import static io.webby.orm.codegen.Joining.*;

@SuppressWarnings("UnnecessaryStringEscape")
public class ModelAdapterCodegen extends BaseCodegen {
    private final AdapterArch adapter;
    private final Map<String, String> mainContext;

    public ModelAdapterCodegen(@NotNull AdapterArch adapter, @NotNull Appendable writer) {
        super(writer);
        this.adapter = adapter;

        this.mainContext = EasyMaps.asMap(
            "$AdapterClass", adapter.javaName(),
            "$ModelClass", Naming.shortCanonicalJavaName(adapter.pojoArch().pojoType())
        );
    }

    public void generateJava() {
        imports();

        classDef();
        staticConst();

        createInstance();

        toValueObject();
        fillArrayValues();
        toNewValuesArray();

        appendLine("}");
    }

    private void imports() {
        List<String> classesToImport = Streams.concat(
            Stream.of(JdbcAdapt.class, getBaseClass()).map(FQN::of),
            getNestedAdapters().stream().map(FQN::of)
        ).filter(fqn -> !isSkippablePackage(fqn.packageName())).map(FQN::importName).sorted().distinct().toList();
        Map<String, String> context = Map.of(
            "$package", adapter.packageName(),
            "$imports", classesToImport.stream().map("import %s;"::formatted).collect(LINE_JOINER)
        );

        appendCode(0, """
        package $package;
        
        import javax.annotation.*;
        $imports\n
        """, context);
    }

    private @NotNull Class<?> getBaseClass() {
        return adapter.pojoArch().isSingleColumn() ? JdbcSingleValueAdapter.class : JdbcArrayAdapter.class;
    }

    private @NotNull List<Class<?>> getNestedAdapters() {
        ArrayList<Class<?>> result = new ArrayList<>();
        adapter.pojoArch().iterateAllFields(field -> {
            if (field instanceof PojoFieldAdapter fieldAdapter) {
                result.add(fieldAdapter.adapterClass());
            }
        });
        return result;
    }

    private boolean isSkippablePackage(@NotNull String packageName) {
        return packageName.equals(adapter.packageName()) || packageName.equals("java.util") || packageName.equals("java.lang");
    }

    private void classDef() {
        PojoArch pojo = adapter.pojoArch();
        Map<String, String> context = Map.of(
            "$BaseClass", getBaseClass().getSimpleName(),
            "$BaseGeneric", "$ModelClass",
            "$names", pojo.columns().stream().map(Column::sqlName).map(JavaSupport::wrapAsStringLiteral).collect(COMMA_JOINER)
        );

        appendCode(0, """
        @JdbcAdapt(value = $ModelClass.class, names = { $names })
        public class $AdapterClass implements $BaseClass<$BaseGeneric> {
        """, EasyMaps.merge(context, mainContext));
    }

    private void staticConst() {
        appendCode("""
        public static final $AdapterClass ADAPTER = new $AdapterClass();\n
        """, mainContext);
    }

    private void createInstance() {
        PojoArch pojo = adapter.pojoArch();
        Map<String, String> context = Map.of(
            "$params", pojo.columns().stream().map(ModelAdapterCodegen::columnToParam).collect(COMMA_JOINER),
            "$constructor", fieldConstructor(pojo, nativeFieldsColumnIndex(pojo), new StringBuilder())
        );

        appendCode("""
        public @Nonnull $ModelClass createInstance($params) {
            return $constructor;
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    // FIX[minor]: use javaVariableName
    private static @NotNull String columnToParam(@NotNull Column column) {
        Class<?> nativeType = column.type().jdbcType().nativeType();
        return "%s %s".formatted(nativeType.getSimpleName(), column.sqlName());
    }

    private static @NotNull Map<PojoField, Column> nativeFieldsColumnIndex(@NotNull PojoArch pojo) {
        Map<PojoField, Column> result = new LinkedHashMap<>();
        pojo.iterateAllFields(field -> {
            if (field instanceof PojoFieldNative fieldNative) {
                assert !result.containsKey(field) :
                    "Internal error. Several columns for one field: `%s` of `%s`: %s, %s"
                    .formatted(field, pojo.pojoType(), fieldNative.column(), result.get(field));
                result.put(field, fieldNative.column());
            }
        });
        return result;
    }

    private static @NotNull String fieldConstructor(@NotNull PojoArch pojo,
                                                    @NotNull Map<PojoField, Column> nativeFieldsColumns,
                                                    @NotNull StringBuilder builder) {
        String canonicalName = Naming.shortCanonicalJavaName(pojo.pojoType());
        builder.append("new ").append(canonicalName).append("(");
        for (PojoField field : pojo.fields()) {
            if (field instanceof PojoFieldNative) {
                builder.append(field.fullSqlName());
            } else if (field instanceof PojoFieldMapper) {
                builder.append(field.mapperApiOrDie().expr().jdbcToField(field.fullSqlName()));
            } else if (field instanceof PojoFieldNested fieldNested) {
                fieldConstructor(fieldNested.pojo(), nativeFieldsColumns, builder);
            } else if (field instanceof PojoFieldAdapter fieldAdapter) {
                String params = fieldAdapter.columns().stream().map(Column::sqlName).collect(COMMA_JOINER);
                builder.append(fieldAdapter.adapterApiOrDie().expr().createInstance(params));
            } else {
                throw new IllegalStateException("Internal error. Unrecognized field: " + field);
            }
            builder.append(", ");
        }
        builder.setLength(builder.length() - 2);  // last comma
        return builder.append(")").toString();
    }

    private void toValueObject() {
        PojoArch pojo = adapter.pojoArch();
        if (pojo.isMultiColumn()) {
            return;
        }

        Map<String, String> context = Map.of(
            "$instance_getter", pojoSingleGetter(pojo)
        );

        appendCode("""
        @Override
        public Object toValueObject($ModelClass instance) {
            return $instance_getter;
        }\n
        """, EasyMaps.merge(context, mainContext));
    }

    private static @NotNull String pojoSingleGetter(@NotNull PojoArch pojo) {
        List<PojoField> fields = pojo.fields();
        assert fields.size() == 1 : "Expected a single pojo field, but found: %s".formatted(fields);

        PojoField field = fields.get(0);
        String accessor = "instance.%s".formatted(field.javaAccessor());
        return switch (field.typeSupport()) {
            case NATIVE -> accessor;
            case MAPPER_API -> field.mapperApiOrDie().expr().fieldToJdbc(accessor);
            case ADAPTER_API -> field.adapterApiOrDie().expr().toValueObject(accessor);
        };
    }

    private void fillArrayValues() {
        PojoArch pojo = adapter.pojoArch();
        if (pojo.isSingleColumn()) {
            return;
        }

        Map<String, String> context = Map.of(
            "$assignments", pojoAssignmentLines(pojo).stream().collect(linesJoiner(INDENT1))
        );

        appendCode("""
        @Override
        public void fillArrayValues(@Nonnull $ModelClass instance, Object[] array, int start) {
        $assignments
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static @NotNull List<String> pojoAssignmentLines(@NotNull PojoArch pojo) {
        ArrayList<String> result = new ArrayList<>();
        int index = 0;
        for (PojoField field : pojo.fields()) {
            String arrayIndex = index == 0 ? "start" : "start+%d".formatted(index);
            String getter = "instance.%s".formatted(field.javaAccessor());

            switch (field.typeSupport()) {
                case NATIVE -> {
                    result.add("array[%s] = %s;".formatted(arrayIndex, getter));
                    index++;
                }
                case MAPPER_API -> {
                    result.add("array[%s] = %s;".formatted(arrayIndex, field.mapperApiOrDie().expr().fieldToJdbc(getter)));
                    index++;
                }
                case ADAPTER_API -> {
                    AdapterApi info = field.adapterApiOrDie();
                    result.add(info.statement().fillArrayValues(getter, "array", arrayIndex));
                    index += info.adapterColumnsNumber();
                }
            }
        }
        return result;
    }

    private void toNewValuesArray() {
        int columnsNumber = adapter.pojoArch().columnsNumber();
        if (columnsNumber == 1) {
            return;
        }

        appendCode("""
        @Override
        public @Nonnull Object[] toNewValuesArray($ModelClass instance) {
            Object[] array = new Object[$columns_number];
            fillArrayValues(instance, array, 0);
            return array;
        }\n
        """, EasyMaps.merge(mainContext, Map.of("$columns_number", String.valueOf(columnsNumber))));
    }
}
