package io.webby.orm.codegen;

import org.jetbrains.annotations.NotNull;

class JavaSupport {
    public static final String INDENT1 = "    ";
    public static final String INDENT2 = "        ";
    public static final String INDENT3 = "            ";

    public static final String EMPTY_LINE = "/* EMPTY */";

    public static @NotNull Snippet wrapAsStringLiteral(@NotNull Snippet snippet) {
        if (snippet.linesNumber() <= 1) {
            return new Snippet().withFormattedLine("\"%s\"", snippet.joinLines());
        } else {
            return wrapAsTextBlock(snippet);
        }
    }

    public static @NotNull String wrapAsStringLiteral(@NotNull Snippet snippet, @NotNull String indent) {
        return wrapAsStringLiteral(snippet).joinLines(indent);
    }

    public static @NotNull String wrapAsStringLiteral(@NotNull String snippet) {
        return "\"%s\"".formatted(snippet);
    }

    public static @NotNull Snippet wrapAsTextBlock(@NotNull Snippet snippet) {
        return new Snippet()
                .withLine("\"\"\"")
                .withLines(snippet)
                .withLine("\"\"\"");
    }

    public static @NotNull String wrapAsTextBlock(@NotNull Snippet snippet, @NotNull String indent) {
        return wrapAsTextBlock(snippet).joinLines(indent);
    }
}
