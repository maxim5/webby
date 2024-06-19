package io.spbx.util.classpath;

import org.jetbrains.annotations.NotNull;

public class RuntimeRequirement {
    public static void verify(@NotNull String className, @NotNull String requirementName) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ignore) {
            throw new UnsupportedOperationException("Runtime requirement is missing: " + requirementName);
        }
    }

    public static void verify(@NotNull String className) {
        verify(className, className);
    }
}
