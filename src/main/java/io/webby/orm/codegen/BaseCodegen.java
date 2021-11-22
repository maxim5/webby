package io.webby.orm.codegen;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static io.webby.orm.codegen.JavaSupport.INDENT1;

@SuppressWarnings("UnusedReturnValue")
public abstract class BaseCodegen {
    protected final Appendable writer;

    protected BaseCodegen(@NotNull Appendable writer) {
        this.writer = writer;
    }

    private BaseCodegen append(@NotNull String value) throws IOException {
        writer.append(value);
        return this;
    }

    private BaseCodegen indent(int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.append(INDENT1);
        }
        return this;
    }

    protected BaseCodegen appendCode(@NotNull String code) throws IOException {
        return appendCode(1, code, Map.of());
    }

    protected BaseCodegen appendCode(@NotNull String code, @NotNull Map<String, String> context) throws IOException {
        return appendCode(1, code, context);
    }

    protected BaseCodegen appendCode(int indent, @NotNull String code, @NotNull Map<String, String> context) throws IOException {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            code = code.replace(entry.getKey(), entry.getValue());
        }
        Stream<String> lines = code.lines();
        for (String line : (Iterable<String>) lines::iterator) {
            indent(indent).appendLine(line);
        }
        return this;
    }

    protected BaseCodegen appendLine(@NotNull String... values) throws IOException {
        for (String value : values) {
            writer.append(value);
        }
        writer.append("\n");
        return this;
    }
}
