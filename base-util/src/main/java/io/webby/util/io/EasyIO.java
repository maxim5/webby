package io.webby.util.io;

import com.google.common.io.Closeables;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public interface EasyIO {
    interface Close {
        static void closeQuietly(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, true);
            } catch (IOException impossible) {
                Unchecked.rethrow(impossible);
            }
        }

        static void closeRethrow(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, false);
            } catch (IOException e) {
                Unchecked.rethrow(e);
            }
        }

        static void closeQuietly(@Nullable AutoCloseable closeable) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception ignore) {
            }
        }

        static void closeRethrow(@Nullable AutoCloseable closeable) {
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
