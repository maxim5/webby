package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.common.ClasspathScanner;
import io.webby.examples.model.PrimitiveModel;
import io.webby.examples.model.StringModel;
import io.webby.examples.model.TimingModel;
import io.webby.examples.model.WrappersModel;
import io.webby.util.TimeIt;
import io.webby.util.sql.DataClassAdaptersLocator;
import io.webby.util.sql.DataTableCodegen;
import io.webby.util.sql.SchemaFactory;
import io.webby.util.sql.schema.TableSchema;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class ExamplesCodegenMain {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final String DESTINATION_DIRECTORY = "src/examples/generated/sql";

    private static final DataClassAdaptersLocator locator = new DataClassAdaptersLocator(new ClasspathScanner());

    public static void main(String[] args) throws Exception {
        List<SchemaFactory.DataClassInput> inputs = List.of(
            new SchemaFactory.DataClassInput(DefaultUser.class, "User"),
            new SchemaFactory.DataClassInput(Session.class),

            new SchemaFactory.DataClassInput(PrimitiveModel.class),
            new SchemaFactory.DataClassInput(StringModel.class),
            new SchemaFactory.DataClassInput(TimingModel.class),
            new SchemaFactory.DataClassInput(WrappersModel.class)
        );

        TimeIt.timeItOrDie(() -> {
            SchemaFactory factory = new SchemaFactory(locator, inputs);
            for (TableSchema tableSchema : factory.buildSchemas()) {
                runGenerate(tableSchema);
            }
        }, millis -> log.at(Level.INFO).log("Generated %d tables in %d millis", inputs.size(), millis));
    }

    private static void runGenerate(@NotNull TableSchema tableSchema) throws IOException {
        Path destinationDir = Path.of(DESTINATION_DIRECTORY, directoryName(tableSchema));
        if (!Files.exists(destinationDir)) {
            boolean success = destinationDir.toFile().mkdirs();
            assert success;
        }

        String fileName = "%s.java".formatted(tableSchema.javaName());
        File destination = destinationDir.resolve(fileName).toFile();

        try (FileWriter writer = new FileWriter(destination)) {
            DataTableCodegen generator = new DataTableCodegen(locator, tableSchema, writer);
            generator.generateJava();
        }

        /*
        JavaTableCompiler compiler = new JavaTableCompiler();
        String classpath = System.getProperty("java.class.path");
        compiler.compile(List.of(destination.getAbsolutePath()), classpath);
        */
    }

    public static @NotNull String directoryName(@NotNull TableSchema tableSchema) {
        return tableSchema.packageName().replaceAll("\\.", "/");
    }
}
