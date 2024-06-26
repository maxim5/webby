package io.spbx.webby.db.codec.standard;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.cursors.IntCursor;
import io.spbx.util.func.ThrowIntSupplier;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.spbx.webby.db.codec.standard.Codecs.*;

public interface IntContainerCodec<T extends IntContainer> extends Codec<T> {
    @Override
    default @NotNull CodecSize size() {
        return CodecSize.minSize(INT32_SIZE);
    }

    @Override
    default int sizeOf(@NotNull IntContainer instance) {
        return instance.size() * INT32_SIZE + INT32_SIZE;
    }

    @Override
    default int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException {
        writeInt32(instance.size(), output);
        for (IntCursor cursor : instance) {
            writeInt32(cursor.value, output);
        }
        return instance.size() * INT32_SIZE + INT32_SIZE;
    }

    @Override
    default @NotNull T readFrom(@NotNull InputStream input, int available) throws IOException {
        int size = readInt32(input);
        return createContainer(size, () -> readInt32(input));
    }

    @NotNull T createContainer(int size, @NotNull ThrowIntSupplier<IOException> reader) throws IOException;
}
