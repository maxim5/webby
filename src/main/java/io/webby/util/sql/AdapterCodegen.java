package io.webby.util.sql;

import io.webby.util.EasyMaps;
import io.webby.util.sql.adapter.JdbcAdapt;
import io.webby.util.sql.schema.AdapterSchema;
import io.webby.util.sql.schema.Naming;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.webby.util.sql.schema.ColumnJoins.LINE_JOINER;

public class AdapterCodegen extends BaseCodegen {
    private final AdapterSchema adapter;
    private final Map<String, String> mainContext;

    public AdapterCodegen(@NotNull AdapterSchema adapter, @NotNull Appendable writer) {
        super(writer);
        this.adapter = adapter;

        this.mainContext = EasyMaps.asMap(
            "$AdapterClass", adapter.javaName(),
            "$DataClass", Naming.shortCanonicalName(adapter.signature().fieldType())
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
        appendCode("""
        public static @Nonnull $DataClass createInstance(int value) {
            // return new $DataClass(value);
            return null;
        }\n
        """, mainContext);
    }

    private void fillArrayValues() throws IOException {
        appendCode("""
        public static void fillArrayValues(@Nonnull $DataClass instance, Object[] array, int start) {
            // array[start] = instance;
        }
        """, mainContext);
    }
}
