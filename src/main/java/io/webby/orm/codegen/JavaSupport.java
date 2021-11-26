package io.webby.orm.codegen;

import org.jetbrains.annotations.NotNull;

class JavaSupport {
    public static final String INDENT1 = "    ";
    public static final String INDENT2 = "        ";
    public static final String INDENT3 = "            ";

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

    public static @NotNull String wrapAsStringLiteral(@NotNull Snippet snippet, @NotNull String indent) {
        return wrapAsStringLiteral(snippet).join(indent);
    }
}
