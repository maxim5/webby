package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.logging.Level;

public class StaticServing {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AppSettings settings;

    @Inject
    public StaticServing(@NotNull AppSettings settings) {
        validateWebPath(settings.webPath());
        this.settings = settings;
    }

    @Nullable
    public ByteBuf getByteBufOrNull(@NotNull String name) throws IOException {
        File file = new File(settings.webPath(), name);
        if (file.exists()) {
            return fileToByteBuf(file);
        }
        return null;
    }

    public byte[] getBytesOrNull(@NotNull String name) throws IOException {
        File file = new File(settings.webPath(), name);
        if (file.exists()) {
            return Files.readAllBytes(file.toPath());
        }
        return null;
    }

    @NotNull
    private static ByteBuf fileToByteBuf(@NotNull File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        return Unpooled.wrappedBuffer(mappedByteBuffer);
    }

    private static void validateWebPath(@Nullable String webPath) {
        if (webPath == null) {
            log.at(Level.WARNING).log("Invalid app settings: static web path is not set");
        } else if (!new File(webPath).exists()) {
            log.at(Level.WARNING).log("Invalid app settings: static web path does not exist: %s", webPath);
        }
    }
}
