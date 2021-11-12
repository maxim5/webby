package io.webby.util.sql.codegen;

import org.jetbrains.annotations.NotNull;

public class JavaSupport {
    public static @NotNull Snippet wrapAsStringLiteral(@NotNull Snippet snippet) {
        if (snippet.linesNumber() <= 1) {
            return new Snippet().withFormattedLine("\"%s\"", snippet.join());
        } else {
            return new Snippet()
                    .withLine("\"\"\"")
                    .withLines(snippet)
                    .withLine("\"\"\"");
        }
    }
}
