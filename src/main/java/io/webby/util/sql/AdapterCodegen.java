package io.webby.util.sql;

import io.webby.util.EasyMaps;
import io.webby.util.sql.adapter.JdbcAdapt;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.*;
import static java.util.Objects.requireNonNull;

public class AdapterCodegen extends BaseCodegen {
    private final AdapterSchema adapter;
    private final Map<String, String> mainContext;

    public AdapterCodegen(@NotNull AdapterSchema adapter, @NotNull Appendable writer) {
        super(writer);
        this.adapter = adapter;

        this.mainContext = EasyMaps.asMap(
            "$AdapterClass", adapter.javaName(),
            "$DataClass", Naming.shortCanonicalName(adapter.pojoSchema().type())
        );
    }

    public void generateJava() throws IOException {
        imports();

        classDef();
        staticConst();

        createInstance();
        fillArrayValues();

        appendLine("}");
    }

    private void imports() throws IOException {
        List<String> classesToImport = Stream.of(JdbcAdapt.class).map(FQN::of).map(FQN::importName).toList();
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

    private void classDef() throws IOException {
        appendCode(0, """
        @JdbcAdapt($DataClass.class)
        public class $AdapterClass {
        """, mainContext);
    }

    private void staticConst() throws IOException {
        appendCode("""
        public static final $AdapterClass ADAPTER = new $AdapterClass();\n
        """, mainContext);
    }

    private void createInstance() throws IOException {
        PojoSchema pojo = adapter.pojoSchema();
        Map<PojoField, Column> columnsPerFields = pojo.columnsPerFields();

        String params = columnsPerFields.values().stream().map(column -> {
            Class<?> nativeType = column.type().jdbcType().nativeType();
            String sqlName = column.sqlName();
            return "%s %s".formatted(nativeType.getSimpleName(), sqlName);
        }).collect(COMMA_JOINER);

        Map<String, String> context = Map.of(
            "$params", params,
            "$constructor", fieldConstructor(pojo, columnsPerFields, new StringBuilder())
        );

        appendCode("""
        public static @Nonnull $DataClass createInstance($params) {
            return $constructor;
        }\n
        """, EasyMaps.merge(mainContext, context));
    }

    private static @NotNull String fieldConstructor(@NotNull PojoSchema pojo,
                                                    @NotNull Map<PojoField, Column> columnsPerFields,
                                                    @NotNull StringBuilder builder) {
        builder.append("new ").append(Naming.shortCanonicalName(pojo.type())).append("(");
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

    private void fillArrayValues() throws IOException {
        Map<String, String> context = Map.of(
            "$assignments", pojoAssignmentLines(adapter.pojoSchema()).stream().collect(linesJoiner(INDENT))
        );

        appendCode("""
        public static void fillArrayValues(@Nonnull $DataClass instance, Object[] array, int start) {
        $assignments
        }
        """, EasyMaps.merge(mainContext, context));
    }

    @NotNull
    private static List<String> pojoAssignmentLines(@NotNull PojoSchema pojo) {
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
                result.add("%s.fillArrayValues(%s, array, %s);".formatted(subPojo.adapterName(), getter, arrayIndex));
                index += subPojo.columnsNumber();
            }
        }
        return result;
    }
}
