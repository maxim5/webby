package io.spbx.util.io;

import com.google.common.io.Closeables;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

import static io.spbx.util.base.EasyCast.castAny;

public class EasyIo {
    public static <T> byte @NotNull [] serialize(@NotNull T instance) {
        return serialize(instance, 8192);
    }

    public static <T> byte @NotNull [] serialize(@NotNull T instance, int bufferSize) {
        assert instance instanceof Serializable : "Object is not Serializable: " + instance;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bufferSize);
             ObjectOutputStream outputStream = new ObjectOutputStream(byteStream)) {
            outputStream.writeObject(instance);
            return byteStream.toByteArray();
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }

    public static <T> @NotNull T deserialize(byte @NotNull [] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ObjectInputStream inputStream = new ObjectInputStream(byteStream)) {
            return castAny(inputStream.readObject());
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        } catch (ClassNotFoundException e) {
            return Unchecked.rethrow(e);
        }
    }

    public static class Close {
        public static void closeQuietly(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, true);
            } catch (IOException impossible) {
                Unchecked.rethrow(impossible);
            }
        }

        public static void closeRethrow(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, false);
            } catch (IOException e) {
                Unchecked.rethrow(e);
            }
        }

        public static void closeQuietly(@Nullable AutoCloseable closeable) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception ignore) {
            }
        }

        public static void closeRethrow(@Nullable AutoCloseable closeable) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                Unchecked.rethrow(e);
            }
        }
    }
}
