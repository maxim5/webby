package io.webby.util.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class EasyFiles {
    public static byte @Nullable [] readAllBytesOrNull(@NotNull Path path) throws IOException {
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }
        return null;
    }

    public static @Nullable ByteBuffer readByteBufferOrNull(@NotNull Path path) throws IOException {
        if (Files.exists(path)) {
            return readFileToByteBuffer(path.toFile());
        }
        return null;
    }

    public static @NotNull ByteBuffer readFileToByteBuffer(@NotNull File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
    }
}
