package io.webby.util.sql;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class BaseGenerator {
    protected final Appendable writer;

    public BaseGenerator(@NotNull Appendable writer) {
        this.writer = writer;
    }

    private BaseGenerator append(@NotNull String value) throws IOException {
        writer.append(value);
        return this;
    }

    private BaseGenerator indent(int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.append("    ");
        }
        return this;
    }

    protected BaseGenerator appendCode(@NotNull String code) throws IOException {
        return appendCode(1, code, Map.of());
    }

    protected BaseGenerator appendCode(@NotNull String code, @NotNull Map<String, String> context) throws IOException {
        return appendCode(1, code, context);
    }

    protected BaseGenerator appendCode(int indent, @NotNull String code, @NotNull Map<String, String> context) throws IOException {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            code = code.replace(entry.getKey(), entry.getValue());
        }
        String[] lines = code.lines().toArray(String[]::new);
        for (String line : lines) {
            indent(indent).appendLine(line);
        }
        return this;
    }

    protected BaseGenerator appendLine(@NotNull String... values) throws IOException {
        for (String value : values) {
            writer.append(value);
        }
        writer.append("\n");
        return this;
    }
}
