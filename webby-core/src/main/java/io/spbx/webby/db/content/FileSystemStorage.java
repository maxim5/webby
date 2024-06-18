package io.spbx.webby.db.content;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.spbx.util.base.OneOf;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class FileSystemStorage implements UserContentStorage {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
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
    public void addFile(@NotNull FileId fileId, byte @NotNull [] content, @NotNull WriteMode mode) throws IOException {
        if (!fileId.isSafe()) {
            throw new SecurityException("File id is unsafe: " + fileId);
        }

        Path path = resolve(fileId);
        if (Files.exists(path)) {
            switch (mode) {
                case FAIL_IF_EXISTS -> throw new IOException("File content already exists: %s".formatted(path));
                case WARN_IF_EXISTS -> log.at(Level.WARNING).log("Content storage already contains file: %s", fileId);
                case INFO_IF_EXISTS -> log.at(Level.INFO).log("Content storage already contains file: %s", fileId);
                case OVERWRITE -> log.at(Level.INFO).log("Overwriting existing file: %s", fileId);
            }
            if (mode != WriteMode.OVERWRITE) {
                return;
            }
        }

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
