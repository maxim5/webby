package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TempDirectoryExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final boolean cleanUp;
    private final boolean onlySuccessful;
    private final List<Path> pathsToClean;
    private Path currentTempDir;

    public TempDirectoryExtension(boolean cleanUp, boolean onlySuccessful) {
        this.cleanUp = cleanUp;
        this.onlySuccessful = onlySuccessful;
        pathsToClean = cleanUp ? new ArrayList<>(1024) : List.of();
    }

    public TempDirectoryExtension() {
        this(true, false);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (cleanUp) {
            cleanUpAtExit();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        String className = context.getRequiredTestClass().getSimpleName();
        String testName = context.getRequiredTestMethod().getName();

        Object[] arguments = JUnitExtensions.extractInvocationArguments(context);
        String format = arguments == null || arguments.length == 0 ?
            "%s.%s.".formatted(className, testName) :
            "%s.%s_%s.".formatted(className, testName, Arrays.stream(arguments).map(Object::toString).collect(Collectors.joining("_")));

        currentTempDir = Files.createTempDirectory(format);
        log.at(Level.INFO).log("Temp storage path for this test: %s", currentTempDir);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        cleanUp(currentTempDir, context.getExecutionException().isEmpty());
    }

    public @NotNull Path getCurrentTempDir() {
        return currentTempDir;
    }

    private void cleanUp(@NotNull Path path, boolean isSuccess) {
        if (cleanUp && (!onlySuccessful || isSuccess)) {
            pathsToClean.add(path);
            log.at(Level.FINE).log("Adding path to clean-up: %s", path);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void deleteAll(@NotNull Path path) {
        log.at(Level.FINE).log("Cleaning-up temp path %s", path);
        try {
            MoreFiles.deleteRecursively(path, RecursiveDeleteOption.ALLOW_INSECURE);
        } catch (Exception e) {
            log.at(Level.INFO).withCause(e).log("Clean-up did not complete successfully");
        }
    }

    private void cleanUpAtExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pathsToClean.forEach(TempDirectoryExtension::deleteAll)));
    }
}
