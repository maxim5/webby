package io.webby.db.content;

import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface UserContentStorage {
    boolean exists(@NotNull FileId fileId);

    void addFileOrDie(@NotNull FileId fileId, byte @NotNull [] content) throws IOException;

    default byte @Nullable [] getFileContent(@NotNull FileId fileId) throws IOException {
        try (InputStream stream = readFileContent(fileId)) {
            return stream != null ? stream.readAllBytes() : null;
        }
    }

    @MustBeClosed
    @Nullable InputStream readFileContent(@NotNull FileId fileId) throws IOException;

    boolean deleteFileContent(@NotNull FileId fileId) throws IOException;
}
