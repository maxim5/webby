package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.User;
import io.webby.common.ClasspathScanner;
import io.webby.db.model.BlobKv;
import io.webby.examples.model.*;
import io.webby.util.base.TimeIt;
import io.webby.orm.codegen.*;
import io.webby.orm.arch.AdapterArch;
import io.webby.orm.arch.JavaNameHolder;
import io.webby.orm.arch.ArchFactory;
import io.webby.orm.arch.TableArch;
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
            new ModelInput(DefaultUser.class, User.class),
            new ModelInput(Session.class),
            new ModelInput(BlobKv.class),

            new ModelInput(PrimitiveModel.class),
            new ModelInput(StringModel.class),
            new ModelInput(TimingModel.class),
            new ModelInput(WrappersModel.class),

            new ModelInput(EnumModel.class),
            new ModelInput(NestedModel.class),
            new ModelInput(DeepNestedModel.class),
            new ModelInput(PojoWithAdapterModel.class),
            new ModelInput(NullableModel.class),
            new ModelInput(InheritedModel.class),
            new ModelInput(ComplexIdModel.class),
            new ModelInput(AtomicModel.class),

            new ModelInput(ForeignKeyModel.InnerInt.class, null, "FKInt"),
            new ModelInput(ForeignKeyModel.InnerLong.class, null, "FKLong"),
            new ModelInput(ForeignKeyModel.InnerString.class, null, "FKString"),
            new ModelInput(ForeignKeyModel.class)
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
