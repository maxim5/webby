package io.webby.db.content;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.webby.db.content.UserContentStorage.WriteMode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.webby.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assertions.*;

public class FileSystemStorageTest {
    private static final byte[] CONTENT = "foo".getBytes();

    private Path root;
    private UserContentStorage storage;

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_new_file(Scenario scenario) throws IOException {
        setup(scenario);
        storage.addFile(new FileId("foo.txt"), CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertTrue(Files.exists(root.resolve("foo.txt")));
        assertBytes(Files.readAllBytes(root.resolve("foo.txt")), CONTENT);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void add_new_directory_and_file(Scenario scenario) throws IOException {
        setup(scenario);
        storage.addFile(new FileId("dir/foo.txt"), CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertTrue(Files.exists(root.resolve("dir/foo.txt")));
        assertBytes(Files.readAllBytes(root.resolve("dir/foo.txt")), CONTENT);
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
        assertTrue(storage.exists(fileId));
        assertEquals(CONTENT.length, storage.getFileSizeInBytes(fileId));
        assertTrue(System.currentTimeMillis() - storage.getLastModifiedMillis(fileId) < 100);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void file_content(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertBytes(storage.readFileContent(fileId).readAllBytes(), CONTENT);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void delete_file(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        storage.addFile(fileId, CONTENT, WriteMode.FAIL_IF_EXISTS);
        assertTrue(storage.deleteFile(fileId));
        assertFalse(storage.exists(fileId));
        assertFalse(Files.exists(root.resolve("foo.txt")));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void delete_file_not_exists(Scenario scenario) throws IOException {
        setup(scenario);
        FileId fileId = new FileId("foo.txt");
        assertFalse(storage.deleteFile(fileId));
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
