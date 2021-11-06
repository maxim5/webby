package io.webby.util.sql.codegen;

import io.webby.util.EasyMaps;
import io.webby.util.sql.adapter.JdbcAdapt;
import io.webby.util.sql.adapter.JdbcArrayAdapter;
import io.webby.util.sql.adapter.JdbcSingleColumnArrayAdapter;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnnecessaryStringEscape")
public class ModelAdapterCodegen extends BaseCodegen {
    private final AdapterSchema adapter;
    private final Map<String, String> mainContext;

    public ModelAdapterCodegen(@NotNull AdapterSchema adapter, @NotNull Appendable writer) {
        super(writer);
        this.adapter = adapter;

        this.mainContext = EasyMaps.asMap(
            "$AdapterClass", adapter.javaName(),
            "$ModelClass", Naming.shortCanonicalName(adapter.pojoSchema().type())
        );
    }

    public void generateJava() throws IOException {
        imports();

        classDef();
        staticConst();

        createInstance();

        toValueObject();
        fillArrayValues();
        toNewValuesArray();

        appendLine("}");
    }

    private void imports() throws IOException {
        List<String> classesToImport = Stream.of(JdbcAdapt.class, getBaseClass())
                .map(FQN::of)
                .map(FQN::importName)
                .sorted()
                .distinct()
                .toList();
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
        return adapter.pojoSchema().columnsNumber() == 1 ? JdbcSingleColumnArrayAdapter.class : JdbcArrayAdapter.class;
    }

    private void classDef() throws IOException {
        Map<String, String> context = Map.of(
            "$BaseClass", getBaseClass().getSimpleName(),
            "$BaseGeneric", "$ModelClass"
        );

        appendCode(0, """
        @JdbcAdapt($ModelClass.class)
        public class $AdapterClass implements $BaseClass<$BaseGeneric> {
        """, EasyMaps.merge(context, mainContext));
    }

    private void staticConst() throws IOException {
        appendCode("""
        public static final $AdapterClass ADAPTER = new $AdapterClass();\n
        """, mainContext);
    }

    private void createInstance() throws IOException {
        PojoSchema pojo = adapter.pojoSchema();
        Map<String, String> context = Map.of(
            "$params", pojo.columns().stream().map(ModelAdapterCodegen::columnToParam).collect(COMMA_JOINER),
            "$constructor", fieldConstructor(pojo, pojo.columnsPerFields(), new StringBuilder())
        );

        appendCode("""
        public @Nonnull $ModelClass createInstance($params) {
            return $constructor;
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static @NotNull String columnToParam(@NotNull Column column) {
        Class<?> nativeType = column.type().jdbcType().nativeType();
        String sqlName = column.sqlName();
        return "%s %s".formatted(nativeType.getSimpleName(), sqlName);
    }

    private static @NotNull String fieldConstructor(@NotNull PojoSchema pojo,
                                                    @NotNull Map<PojoField, Column> columnsPerFields,
                                                    @NotNull StringBuilder builder) {
        String canonicalName = Naming.shortCanonicalName(pojo.type());

        if (pojo.isEnum()) {
            String name = columnsPerFields.get(pojo.fields().get(0)).sqlName();
            return builder.append(canonicalName).append(".values()[").append(name).append("]").toString();
        }

        builder.append("new ").append(canonicalName).append("(");
        boolean isFirst = true;
        for (PojoField field : pojo.fields()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            if (field.isNativelySupported()) {
                String name = columnsPerFields.get(field).sqlName();
                builder.append(name);
            } else {
                fieldConstructor(requireNonNull(field.pojo()), columnsPerFields, builder);
            }
        }
        return builder.append(")").toString();
    }

    private void toValueObject() throws IOException {
        PojoSchema pojo = adapter.pojoSchema();
        if (pojo.columnsNumber() != 1) {
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

    private static @NotNull String pojoSingleGetter(@NotNull PojoSchema pojo) {
        List<PojoField> fields = pojo.fields();
        assert fields.size() == 1 : "Expected a single pojo field, but found: %s".formatted(fields);

        PojoField field = fields.get(0);
        String getter = "instance.%s()".formatted(field.getter().getName());
        if (field.isNativelySupported()) {
            return getter;
        } else {
            PojoSchema subPojo = requireNonNull(field.pojo());
            AdapterInfo info = AdapterInfo.ofSignature(subPojo);
            return "%s.toValueObject(%s);".formatted(info.staticRef(), getter);
        }
    }

    private void fillArrayValues() throws IOException {
        PojoSchema pojo = adapter.pojoSchema();
        if (pojo.columnsNumber() == 1) {
            return;
        }

        Map<String, String> context = Map.of(
            "$assignments", pojoAssignmentLines(pojo).stream().collect(linesJoiner(INDENT))
        );

        appendCode("""
        @Override
        public void fillArrayValues(@Nonnull $ModelClass instance, Object[] array, int start) {
        $assignments
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static @NotNull List<String> pojoAssignmentLines(@NotNull PojoSchema pojo) {
        ArrayList<String> result = new ArrayList<>();
        int index = 0;
        for (PojoField field : pojo.fields()) {
            String arrayIndex = index == 0 ? "start" : "start+%d".formatted(index);
            String getter = "instance.%s()".formatted(field.getter().getName());

            if (field.isNativelySupported()) {
                result.add("array[%s] = %s;".formatted(arrayIndex, getter));
                index++;
            } else {
                PojoSchema subPojo = requireNonNull(field.pojo());
                AdapterInfo info = AdapterInfo.ofSignature(subPojo);
                result.add("%s.fillArrayValues(%s, array, %s);".formatted(info.staticRef(), getter, arrayIndex));
                index += subPojo.columnsNumber();
            }
        }
        return result;
    }

    private void toNewValuesArray() throws IOException {
        int columnsNumber = adapter.pojoSchema().columnsNumber();
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