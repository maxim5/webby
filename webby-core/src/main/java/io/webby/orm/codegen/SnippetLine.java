package io.webby.orm.codegen;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class SnippetLine {
    private final List<String> pieces;

    public SnippetLine() {
        pieces = new ArrayList<>();
    }

    public SnippetLine(@NotNull List<String> pieces) {
        this.pieces = pieces;
    }

    public static @NotNull SnippetLine of(@NotNull String ... pieces) {
        return new SnippetLine(List.of(pieces));
    }

    public static @NotNull SnippetLine of(@NotNull Object ... pieces) {
        return new SnippetLine(Stream.of(pieces).map(Object::toString).toList());
    }

    @CheckReturnValue
    public @NotNull String join() {
        return String.join("", pieces);
    }

    @CheckReturnValue
    public @NotNull String join(@NotNull String separator) {
        return String.join(separator, pieces);
    }

    @CheckReturnValue
    public @NotNull String joinNonEmpty(@NotNull String separator) {
        return String.join(separator, pieces.stream().filter(s -> !s.isEmpty()).toList());
    }

    @Override
    public String toString() {
        return join();
    }
}
