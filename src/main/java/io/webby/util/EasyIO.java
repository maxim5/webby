package io.webby.util;

import com.google.common.io.Closeables;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public interface EasyIO {
    interface Close {
        @SuppressWarnings("UnstableApiUsage")
        static void closeQuietly(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, true);
            } catch (IOException impossible) {
                Rethrow.rethrow(impossible);
            }
        }

        @SuppressWarnings("UnstableApiUsage")
        static void closeRethrow(@Nullable Closeable closeable) {
            try {
                Closeables.close(closeable, false);
            } catch (IOException e) {
                Rethrow.rethrow(e);
            }
        }
    }
}
