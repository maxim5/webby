package io.spbx.webby.db.content;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record FileId(@NotNull String path) {
    public static @NotNull FileId flatFileId(@NotNull ContentId contentId,
                                             @NotNull FileFormat format,
                                             @NotNull FileExt ext) {
        return new FileId("%s.%s%s".formatted(contentId.contentId(), format.form(), ext.extension()));
    }

    public static @NotNull FileId nestedFileId(@NotNull String dir,
                                               @NotNull ContentId contentId,
                                               @NotNull FileFormat format,
                                               @NotNull FileExt ext) {
        assert !dir.isEmpty() : "Directory is empty";
        return new FileId("%s/%s.%s%s".formatted(dir, contentId.contentId(), format.form(), ext.extension()));
    }

    public @NotNull ContentId parseContentIdOrDie() {
        Path path = Path.of(this.path);
        String fileName = path.getFileName().toString();
        String contentId = fileName.substring(0, fileName.indexOf('.'));
        return new ContentId(contentId);
    }

    public boolean isSafe() {
        Path path = Path.of(this.path);
        return !path.isAbsolute() && !isAbsoluteLike(this.path) && path.normalize().equals(path) && !hasDotDot(this.path);
    }

    private static boolean isAbsoluteLike(@NotNull String path) {
        return path.contains(":") || path.startsWith("/") || path.startsWith("~");
    }

    private static boolean hasDotDot(@NotNull String path) {
        return path.contains("..") && (path.startsWith("..") || path.contains("/..") || path.contains("\\.."));
    }
}
