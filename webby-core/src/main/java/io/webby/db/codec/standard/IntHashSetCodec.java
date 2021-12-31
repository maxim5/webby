package io.webby.db.codec.standard;

import com.carrotsearch.hppc.IntHashSet;
import io.webby.util.func.ThrowIntSupplier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class IntHashSetCodec implements IntContainerCodec<IntHashSet> {
    public static final IntHashSetCodec INSTANCE = new IntHashSetCodec();

    @Override
    public @NotNull IntHashSet createContainer(int size, @NotNull ThrowIntSupplier<IOException> reader) throws IOException {
        IntHashSet hashSet = new IntHashSet(size);
        for (int i = 0; i < size; i++) {
            hashSet.add(reader.getAsInt());
        }
        return hashSet;
    }
}
