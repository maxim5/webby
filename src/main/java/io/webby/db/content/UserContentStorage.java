package io.webby.db.content;

import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public interface UserContentStorage {
    @NotNull UserContentLocation getLocation(@NotNull FileId fileId);

    boolean exists(@NotNull FileId fileId);

    void addFileOrDie(@NotNull FileId fileId, byte @NotNull [] content) throws IOException;

    long getFileSizeInBytes(@NotNull FileId fileId) throws IOException;

    long getLastModifiedMillis(@NotNull FileId fileId) throws IOException;

    @MustBeClosed
    @NotNull InputStream readFileContent(@NotNull FileId fileId) throws IOException;

    boolean deleteFile(@NotNull FileId fileId) throws IOException;
}
