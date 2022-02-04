package io.webby.db.content;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemStorage implements UserContentStorage {
    private final Path root;

    @Inject
    public FileSystemStorage(@NotNull Settings settings) {
        this(settings.userContentPath());
    }

    @VisibleForTesting
    public FileSystemStorage(@NotNull Path root) {
        this.root = root;
    }

    @Override
    public @NotNull UserContentLocation getLocation(@NotNull FileId fileId) {
        return new UserContentLocation(OneOf.ofFirst(resolve(fileId)));
    }

    @Override
    public boolean exists(@NotNull FileId fileId) {
        return Files.exists(resolve(fileId));
    }

    @Override
    public void addFileOrDie(@NotNull FileId fileId, byte @NotNull [] content) throws IOException {
        assert fileId.isSafe() : "File id is unsafe: " + fileId;
        Path path = resolve(fileId);
        assert !Files.exists(path) : "File content already exists: %s".formatted(path);
        Files.createDirectories(path.getParent());
        Files.write(path, content);
    }

    @Override
    public long getFileSizeInBytes(@NotNull FileId fileId) throws IOException {
        return Files.size(resolve(fileId));
    }

    @Override
    public long getLastModifiedMillis(@NotNull FileId fileId) throws IOException {
        Path path = resolve(fileId);
        return Files.getLastModifiedTime(path).toMillis();
    }

    @Override
    public @NotNull InputStream readFileContent(@NotNull FileId fileId) throws IOException {
        Path path = resolve(fileId);
        return Files.newInputStream(path);
    }

    @Override
    public boolean deleteFile(@NotNull FileId fileId) throws IOException {
        Path path = resolve(fileId);
        if (Files.exists(path)) {
            Files.delete(path);
            return true;
        }
        return false;
    }

    private @NotNull Path resolve(@NotNull FileId fileId) {
        return root.resolve(fileId.path());
    }
}
