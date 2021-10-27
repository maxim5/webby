package io.webby.examples.model;

import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.common.ClasspathScanner;
import io.webby.util.TimeIt;
import io.webby.util.sql.DataClassAdaptersLocator;
import io.webby.util.sql.DataTableCodegen;
import io.webby.util.sql.SchemaFactory;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExamplesCodegenRunner {
    private static final String DESTINATION_DIRECTORY = "src/examples/generated/sql";

    private static final DataClassAdaptersLocator locator = new DataClassAdaptersLocator(new ClasspathScanner());

    public static void main(String[] args) throws Exception {
        SchemaFactory factory = new SchemaFactory(locator, List.of(
            new SchemaFactory.DataClassInput(DefaultUser.class, "User"),
            new SchemaFactory.DataClassInput(Session.class)
        ));

        for (TableSchema tableSchema : factory.buildSchemas()) {
            rungGenerate(tableSchema);
        }
    }

    private static void rungGenerate(@NotNull TableSchema tableSchema) throws IOException {
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

    private static void misc() throws IOException {
        String[] files = {
                "src/main/java/io/webby/util/AnyLog.java",
                "src/main/java/io/webby/util/AtomicLazy.java",
                "src/main/java/io/webby/util/EasyCast.java",
                "src/main/java/io/webby/util/EasyIO.java",
                "src/main/java/io/webby/util/EasyIterables.java",
        };

        TimeIt.timeItOrDie(() -> {
            // for (int i = 0; i < 10; i++)
            for (String file : files) {
                Files.getLastModifiedTime(Path.of(file));
            }
        }, value -> System.out.println(value));
    }
}
