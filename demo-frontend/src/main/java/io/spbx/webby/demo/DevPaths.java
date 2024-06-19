package io.spbx.webby.demo;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class DevPaths {
    public static final String PROJECT_HOME = locateProjectHome();
    public static final String DEMO_HOME = PROJECT_HOME + "demo-frontend/";
    public static final String DEMO_RESOURCES = DEMO_HOME + "src/main/resources/";
    public static final String DEMO_WEB = DEMO_RESOURCES + "web/";

    private static @NotNull String locateProjectHome() {
        Path workingDir = Path.of(".").toAbsolutePath();
        while (workingDir != null && !Files.exists(workingDir.resolve(".infra"))) {
            workingDir = workingDir.getParent();
        }
        return (workingDir != null ? workingDir.toString() : ".") + "/";
    }
}
