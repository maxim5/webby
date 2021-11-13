package io.webby.util.sql.codegen;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class Snippet {
    private final List<String> lines = new ArrayList<>();

    public @NotNull Snippet withLine(@NotNull String line) {
        this.lines.add(line);
        return this;
    }

    public @NotNull Snippet withLine(@NotNull String linePart1, @NotNull String linePart2) {
        return withLine(linePart1 + linePart2);
    }

    public @NotNull Snippet withLine(@NotNull String linePart1, @NotNull String linePart2, @NotNull String linePart3) {
        return withLine(linePart1 + linePart2 + linePart3);
    }

    public @NotNull Snippet withLine(@NotNull String @NotNull ... lineParts) {
        return withLine(String.join("", lineParts));
    }

    public @NotNull Snippet withFormattedLine(@NotNull String line, @NotNull Object @NotNull ... args) {
        return withLine(line.formatted(args));
    }

    public @NotNull Snippet withLines(@NotNull Collection<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    public @NotNull Snippet withLines(@NotNull Snippet snippet) {
        return withLines(snippet.lines);
    }

    public int linesNumber() {
        return lines.size();
    }

    public @NotNull List<String> lines() {
        return lines;
    }

    public @NotNull String join() {
        return String.join("\n", lines);
    }

    public @NotNull String join(@NotNull String indent) {
        return lines.stream().collect(Collectors.joining("\n" + indent, "", ""));
    }

    @Override
    public String toString() {
        return join();
    }
}
