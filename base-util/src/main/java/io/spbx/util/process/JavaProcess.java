package io.spbx.util.process;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * https://dzone.com/articles/running-a-java-class-as-a-subprocess
 */
public class JavaProcess {
    public static @NotNull ProcessBuilder startJvm(@NotNull Class<?> klass,
                                                   @NotNull List<String> jvmArgs,
                                                   @NotNull List<String> args) {
        String javaBin = ProcessHandle.current().info().commandLine().orElseGet(JavaProcess::defaultJavaBinary);
        String classpath = currentJvmClasspath();
        String className = klass.getName();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(args);

        return new ProcessBuilder(command);
    }

    public static @NotNull String currentJvmClasspath() {
        return System.getProperty("java.class.path");
    }

    public static @NotNull List<String> currentJvmArguments() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    private static @NotNull String defaultJavaBinary() {
        String javaHome = System.getProperty("java.home");
        return "%s%sbin%sjava".formatted(javaHome, File.separator, File.separator);
    }

    public @NotNull String toClasspath(@NotNull Iterable<? extends CharSequence> classpath) {
        return String.join(File.pathSeparator, classpath);
    }

    public @NotNull String toClasspath(@NotNull String @NotNull ... classpath) {
        return String.join(File.pathSeparator, classpath);
    }
}
