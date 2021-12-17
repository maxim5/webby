package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.common.ClasspathScanner;
import io.webby.db.model.BlobKv;
import io.webby.examples.model.*;
import io.webby.orm.arch.AdapterArch;
import io.webby.orm.arch.ArchFactory;
import io.webby.orm.arch.JavaNameHolder;
import io.webby.orm.arch.TableArch;
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
import java.util.List;
import java.util.logging.Level;

public class ExamplesCodegenMain {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final String DESTINATION_DIRECTORY = "src/examples/generated/sql";

    private static final ModelAdaptersScanner locator = new ModelAdaptersScanner(new AppSettings(), new ClasspathScanner());

    public static void main(String[] args) throws Exception {
        List<ModelInput> inputs = List.of(
            ModelInput.of(DefaultUser.class),
            ModelInput.of(Session.class),
            ModelInput.of(BlobKv.class),

            ModelInput.of(PrimitiveModel.class),
            ModelInput.of(StringModel.class),
            ModelInput.of(TimingModel.class),
            ModelInput.of(WrappersModel.class),

            ModelInput.of(EnumModel.class),
            ModelInput.of(NestedModel.class),
            ModelInput.of(DeepNestedModel.class),
            ModelInput.of(PojoWithAdapterModel.class),
            ModelInput.of(NullableModel.class),
            ModelInput.of(InheritedModel.class),
            ModelInput.of(ComplexIdModel.class),
            ModelInput.of(AtomicModel.class),

            ModelInput.of(ForeignKeyModel.InnerInt.class),
            ModelInput.of(ForeignKeyModel.InnerLong.class),
            ModelInput.of(ForeignKeyModel.InnerString.class),
            ModelInput.of(ForeignKeyModel.class)
        );

        TimeIt.timeItOrDie(() -> {
            ArchFactory factory = new ArchFactory(locator, inputs);
            factory.build();

            for (AdapterArch adapterArch : factory.getAdapterArches()) {
                runGenerate(adapterArch);
            }
            for (TableArch tableArch : factory.getTableArches()) {
                runGenerate(tableArch);
            }
            return factory;
        }, (factory, millis) -> {
            int adapters = factory.getAdapterArches().size();
            int tables = factory.getTableArches().size();
            log.at(Level.INFO).log("Generated %d adapters and %d tables in %d millis", adapters, tables, millis);
        });
    }

    private static void runGenerate(@NotNull AdapterArch adapter) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(adapter))) {
            ModelAdapterCodegen generator = new ModelAdapterCodegen(adapter, writer);
            generator.generateJava();
        }
    }

    private static void runGenerate(@NotNull TableArch table) throws IOException {
        try (FileWriter writer = new FileWriter(getDestinationFile(table))) {
            ModelTableCodegen generator = new ModelTableCodegen(locator, table, writer);
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
