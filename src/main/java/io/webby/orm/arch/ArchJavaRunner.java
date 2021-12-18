package io.webby.orm.arch;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppSettings;
import io.webby.common.ClasspathScanner;
import io.webby.orm.codegen.ModelAdapterCodegen;
import io.webby.orm.codegen.ModelAdaptersScanner;
import io.webby.orm.codegen.ModelInput;
import io.webby.orm.codegen.ModelTableCodegen;
import io.webby.util.base.TimeIt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class ArchJavaRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final ModelAdaptersScanner locator;
    private String destination;

    public ArchJavaRunner(@NotNull ModelAdaptersScanner locator) {
        this.locator = locator;
    }

    public ArchJavaRunner() {
        this(new ModelAdaptersScanner(new AppSettings(), new ClasspathScanner()));
    }

    public void runGenerate(@NotNull String destinationDirectory, @NotNull Iterable<ModelInput> inputs) throws IOException {
        destination = destinationDirectory;
        TimeIt.timeItOrDie(() -> {
            ArchFactory factory = new ArchFactory(locator, inputs);
            factory.build();

            for (AdapterArch adapterArch : factory.getAdapterArches()) {
                generate(adapterArch);
            }
            for (TableArch tableArch : factory.getTableArches()) {
                generate(tableArch);
            }
            return factory;
        }, (factory, millis) -> {
            int adapters = factory.getAdapterArches().size();
            int tables = factory.getTableArches().size();
            log.at(Level.INFO).log("Generated %d adapters and %d tables in %d millis", adapters, tables, millis);
        });
    }

    private void generate(@NotNull AdapterArch adapter) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(adapter))) {
            ModelAdapterCodegen generator = new ModelAdapterCodegen(adapter, writer);
            generator.generateJava();
        }
    }

    private void generate(@NotNull TableArch table) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(table))) {
            ModelTableCodegen generator = new ModelTableCodegen(locator, table, writer);
            generator.generateJava();
        }
    }

    private @NotNull File getDestinationFile(@NotNull JavaNameHolder named) {
        String directoryName = named.packageName().replaceAll("\\.", "/");
        Path destinationDir = Path.of(destination, directoryName);
        if (!Files.exists(destinationDir)) {
            boolean success = destinationDir.toFile().mkdirs();
            assert success : "Failed to create destination directory: %s".formatted(destinationDir);
        }
        String fileName = "%s.java".formatted(named.javaName());
        return destinationDir.resolve(fileName).toFile();
    }
}
