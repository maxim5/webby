package io.spbx.webby.db.content;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.spbx.webby.db.content.FileId;
import io.spbx.webby.db.content.FileSystemStorage;
import io.spbx.webby.db.content.UserContentStorage;
import io.spbx.webby.db.content.UserContentStorage.WriteMode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileSystemStorageTest {
    private static final byte[] CONTENT = "foo".getBytes();

    private Path root;
    private UserContentStorage storage;

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_new_file(Scenario scenario) throws IOException {
        setup(scenario);
        storage.addFile(new FileId("foo.txt"), CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertThat(Files.exists(root.resolve("foo.txt"))).isTrue();
        assertBytes(Files.readAllBytes(root.resolve("foo.txt"))).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_new_directory_and_file(Scenario scenario) throws IOException {
        setup(scenario);
        storage.addFile(new FileId("dir/foo.txt"), CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertThat(Files.exists(root.resolve("dir/foo.txt"))).isTrue();
        assertBytes(Files.readAllBytes(root.resolve("dir/foo.txt"))).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_file_already_exists_fails(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertThrows(Throwable.class, () -> storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_file_unsafe_fails(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("../foo.txt");
        assertThrows(Throwable.class, () -> storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void file_stats(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertThat(storage.exists(fileId)).isTrue();
        assertThat(storage.getFileSizeInBytes(fileId)).isEqualTo(CONTENT.length);
        assertThat(System.currentTimeMillis() - storage.getLastModifiedMillis(fileId) < 100).isTrue();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void file_content(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertBytes(storage.readFileContent(fileId).readAllBytes()).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void delete_file(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertThat(storage.deleteFile(fileId)).isTrue();
        assertThat(storage.exists(fileId)).isFalse();
        assertThat(Files.exists(root.resolve("foo.txt"))).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void delete_file_not_exists(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        assertThat(storage.deleteFile(fileId)).isFalse();
    }

    private void setup(@NotNull Scenario scenario) throws IOException {
        FileSystem fileSystem = switch (scenario) {
            case UNIX -> Jimfs.newFileSystem(Configuration.unix());
            case WINDOWS -> Jimfs.newFileSystem(Configuration.windows());
            case OSX -> Jimfs.newFileSystem(Configuration.osX());
        };
        root = switch (scenario) {
            case UNIX -> fileSystem.getPath("/home/foo/");
            case WINDOWS -> fileSystem.getPath("C:/foo/");
            case OSX -> fileSystem.getPath("/Users/foo/");
        };
        Files.createDirectories(root);
        storage = new FileSystemStorage(root);
    }

    private enum Scenario {
        UNIX,
        WINDOWS,
        OSX,
    }
}
