package io.webby.db.event;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.procedures.IntProcedure;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.webby.db.codec.Codecs.*;

public class IntHashSetCodec implements Codec<IntHashSet> {
    @Override
    public @NotNull CodecSize size() {
        return CodecSize.minSize(INT32_SIZE);
    }

    @Override
    public int sizeOf(@NotNull IntHashSet instance) {
        return instance.size() * INT32_SIZE + INT32_SIZE;
    }

    @Override
    public int writeTo(@NotNull OutputStream output, @NotNull IntHashSet instance) throws IOException {
        writeInt32(instance.size(), output);
        instance.forEach((IntProcedure) value -> {
            try {
                writeInt32(value, output);
            } catch (IOException e) {
                Rethrow.rethrow(e);
            }
        });
        return instance.size() * INT32_SIZE + INT32_SIZE;
    }

    @Override
    public @NotNull IntHashSet readFrom(@NotNull InputStream input, int available) throws IOException {
        int size = readInt32(input);
        IntHashSet hashSet = new IntHashSet(size);
        for (int i = 0; i < size; i++) {
            int value = readInt32(input);
            hashSet.add(value);
        }
        return hashSet;
    }
}
