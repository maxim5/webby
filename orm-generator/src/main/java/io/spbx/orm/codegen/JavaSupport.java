package io.spbx.orm.codegen;

import io.spbx.util.guava.SourceCodeEscapers;
import org.jetbrains.annotations.NotNull;

class JavaSupport {
    public static final String EMPTY_LINE = "/* EMPTY */";

    public static @NotNull Snippet wrapAsStringLiteral(@NotNull Snippet snippet) {
        if (snippet.isBlock()) {
            return wrapAsTextBlock(snippet);
        } else {
            return wrapAsSingleLineLiteral(snippet);
        }
    }

    public static @NotNull String wrapAsStringLiteral(@NotNull String snippet) {
        return wrapAsStringLiteral(new Snippet().withMultiline(snippet)).joinLines();
    }

    private static @NotNull Snippet wrapAsSingleLineLiteral(@NotNull Snippet snippet) {
        return new Snippet().withFormattedLine("\"%s\"", snippet.map(JavaSupport::escapeJavaStringLiteral).joinLines());
    }

    private static @NotNull Snippet wrapAsTextBlock(@NotNull Snippet snippet) {
        return new Snippet()
            .withLine("\"\"\"")
            .withLines(snippet.map(JavaSupport::escapeJavaStringLiteral))
            .withLine("\"\"\"");
    }

    private static @NotNull String escapeJavaStringLiteral(@NotNull String str) {
        return SourceCodeEscapers.javaCharEscaper().escape(str);
    }
}
