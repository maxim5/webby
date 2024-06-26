package io.spbx.orm.codegen;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CheckReturnValue;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ResultOfMethodCallIgnored")
@CheckReturnValue
class Snippet {
    private final List<String> lines = new ArrayList<>();
    private boolean isBlockForced = false;

    public boolean isBlock() {
        return isBlockForced || linesNumber() > 1;
    }

    public int linesNumber() {
        return lines.size();
    }

    public @NotNull ImmutableList<String> lines() {
        return ImmutableList.copyOf(lines);
    }

    public @NotNull Snippet withLine(@NotNull String line) {
        assert !containsNewLineChars(line) : "Line is not a single line: " + line;
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
        lines.forEach(this::withLine);
        return this;
    }

    public @NotNull Snippet withLines(@NotNull Stream<String> lines) {
        lines.forEach(this::withLine);
        return this;
    }

    public @NotNull Snippet withLines(@NotNull String @NotNull [] lines) {
        return withLines(Arrays.stream(lines));
    }

    public @NotNull Snippet withLines(@NotNull Snippet snippet) {
        return withLines(snippet.lines());
    }

    public @NotNull Snippet withMultiline(@NotNull String multiline) {
        return withLines(multiline.lines()).withForceBlock(containsNewLineChars(multiline));
    }

    public @NotNull Snippet withFormattedMultiline(@NotNull String multiline, @NotNull Object @NotNull ... args) {
        return withMultiline(multiline.formatted(args));
    }

    public @NotNull Snippet withMultilines(@NotNull Collection<String> multilines) {
        multilines.forEach(this::withMultiline);
        return this;
    }

    public @NotNull Snippet withMultilines(@NotNull Stream<String> multilines) {
        multilines.forEach(this::withMultiline);
        return this;
    }

    public @NotNull Snippet withMultilines(@NotNull String @NotNull [] multilines) {
        return withMultilines(Arrays.stream(multilines));
    }

    public @NotNull Snippet withForceBlock(boolean block) {
        isBlockForced = isBlockForced || block;
        return this;
    }

    @Pure
    public @NotNull Snippet map(@NotNull Function<String, String> convert) {
        return new Snippet().withLines(lines.stream().map(convert));
    }

    @Pure
    public @NotNull String joinLines() {
        return join("\n");
    }

    @Pure
    public @NotNull String joinLines(@NotNull Indent indent) {
        return join(Collectors.joining(indent.delimiter(), "", ""));
    }

    @Pure
    public @NotNull String join(@NotNull String delimiter) {
        return String.join(delimiter, lines);
    }

    @Pure
    public @NotNull String join(@NotNull Collector<CharSequence, ?, String> collector) {
        return lines.stream().collect(collector);
    }

    @Override
    public String toString() {
        return joinLines();
    }

    private static boolean containsNewLineChars(@NotNull String text) {
        return text.contains("\n") || text.contains("\r");
    }
}
