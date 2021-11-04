package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.common.ClasspathScanner;
import io.webby.examples.model.*;
import io.webby.util.TimeIt;
import io.webby.util.sql.AdapterCodegen;
import io.webby.util.sql.DataClassAdaptersLocator;
import io.webby.util.sql.DataTableCodegen;
import io.webby.util.sql.SchemaFactory;
import io.webby.util.sql.schema.AdapterSchema;
import io.webby.util.sql.schema.JavaNameHolder;
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
            new SchemaFactory.DataClassInput(WrappersModel.class),

            new SchemaFactory.DataClassInput(InnerDataModel.class)
        );

        TimeIt.timeItOrDie(() -> {
            SchemaFactory factory = new SchemaFactory(locator, inputs);
            factory.build();

            for (AdapterSchema adapterSchema : factory.getAdapterSchemas()) {
                runGenerate(adapterSchema);
            }
            for (TableSchema tableSchema : factory.getTableSchemas()) {
                runGenerate(tableSchema);
            }
            return factory;
        }, (schema, millis) -> {
            int adapters = schema.getAdapterSchemas().size();
            int tables = schema.getTableSchemas().size();
            log.at(Level.INFO).log("Generated %d adapters and %d tables in %d millis", adapters, tables, millis);
        });
    }

    private static void runGenerate(@NotNull AdapterSchema adapterSchema) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(adapterSchema))) {
            AdapterCodegen generator = new AdapterCodegen(adapterSchema, writer);
            generator.generateJava();
        }
    }

    private static void runGenerate(@NotNull TableSchema tableSchema) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(tableSchema))) {
            DataTableCodegen generator = new DataTableCodegen(locator, tableSchema, writer);
            generator.generateJava();
        }
    }

    @NotNull
    private static File getDestinationFile(@NotNull JavaNameHolder named) {
        String directoryName = named.packageName().replaceAll("\\.", "/");
        Path destinationDir = Path.of(DESTINATION_DIRECTORY, directoryName);
        if (!Files.exists(destinationDir)) {
            boolean success = destinationDir.toFile().mkdirs();
            assert success : "Failed to create destination directory: %s".formatted(destinationDir);
        }
        String fileName = "%s.java".formatted(named.javaName());
        return destinationDir.resolve(fileName).toFile();
    }
}
