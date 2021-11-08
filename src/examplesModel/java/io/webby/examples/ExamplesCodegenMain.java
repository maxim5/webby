package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.common.ClasspathScanner;
import io.webby.examples.model.*;
import io.webby.util.base.TimeIt;
import io.webby.util.sql.codegen.*;
import io.webby.util.sql.schema.AdapterSchema;
import io.webby.util.sql.schema.JavaNameHolder;
import io.webby.util.sql.schema.ModelSchemaFactory;
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

    private static final ModelAdaptersLocator locator = new ModelAdaptersLocator(new ClasspathScanner());

    public static void main(String[] args) throws Exception {
        List<ModelClassInput> inputs = List.of(
            new ModelClassInput(DefaultUser.class, "User"),
            new ModelClassInput(Session.class),

            new ModelClassInput(PrimitiveModel.class),
            new ModelClassInput(StringModel.class),
            new ModelClassInput(TimingModel.class),
            new ModelClassInput(WrappersModel.class),

            new ModelClassInput(EnumModel.class),
            new ModelClassInput(NestedModel.class),
            new ModelClassInput(DeepNestedModel.class),
            new ModelClassInput(PojoWithAdapterModel.class),
            new ModelClassInput(NullableModel.class)
        );

        TimeIt.timeItOrDie(() -> {
            ModelSchemaFactory factory = new ModelSchemaFactory(locator, inputs);
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
            ModelAdapterCodegen generator = new ModelAdapterCodegen(adapterSchema, writer);
            generator.generateJava();
        }
    }

    private static void runGenerate(@NotNull TableSchema tableSchema) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(tableSchema))) {
            ModelTableCodegen generator = new ModelTableCodegen(locator, tableSchema, writer);
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
