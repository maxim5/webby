package io.spbx.webby.demo;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class DevPaths {
    public static final Path PROJECT_HOME = locateProjectHome();
    public static final Path DEMO_HOME = PROJECT_HOME.resolve("demo-frontend");
    public static final Path DEMO_RESOURCES = DEMO_HOME.resolve("src/main/resources");
    public static final Path DEMO_WEB = DEMO_RESOURCES.resolve("web");

    private static @NotNull Path locateProjectHome() {
        Path currentPath = Path.of(".").toAbsolutePath();
        Path workingDir = currentPath;
        while (workingDir != null && !Files.exists(workingDir.resolve(".infra"))) {
            workingDir = workingDir.getParent();
        }
        return workingDir != null ? workingDir : currentPath;
    }
}
