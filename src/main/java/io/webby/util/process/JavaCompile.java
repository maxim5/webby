package io.webby.util.process;

import org.jetbrains.annotations.NotNull;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaCompile {
    public static void compile(@NotNull List<String> files, @NotNull List<String> compileArgs, @NotNull String classpath) {
        List<String> args = new ArrayList<>(files.size() + 8);

        args.addAll(compileArgs);
        args.add("-encoding");
        args.add("UTF-8");
        args.add("-parameters");

        if (!classpath.isEmpty()) {
            args.add("-classpath");
            args.add(classpath);
        }

        args.addAll(files);

        runCompiler(args.toArray(new String[0]));
    }

    public static void runCompiler(String[] args) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        int result = compiler.run(null, null, new PrintStream(errorStream, true, StandardCharsets.UTF_8), args);
        if (result != 0) {
            String errors = errorStream.toString(StandardCharsets.UTF_8);
            throw new RuntimeException("Compilation failed: errors=%s".formatted(errors));
        }
    }
}
