package io.spbx.webby.db.codec.standard;

import com.carrotsearch.hppc.IntArrayList;
import io.spbx.util.func.ThrowIntSupplier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class IntArrayListCodec implements IntContainerCodec<IntArrayList> {
    public static final IntArrayListCodec INSTANCE = new IntArrayListCodec();

    @Override
    public @NotNull IntArrayList createContainer(int size, @NotNull ThrowIntSupplier<IOException> reader) throws IOException {
        IntArrayList arrayList = new IntArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(reader.getAsInt());
        }
        return arrayList;
    }
}
