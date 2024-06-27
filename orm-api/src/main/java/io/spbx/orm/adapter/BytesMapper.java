package io.spbx.orm.adapter;

import io.spbx.util.func.Reversible;
import io.spbx.util.io.EasyIo;
import org.jetbrains.annotations.NotNull;

public class BytesMapper<T> implements Reversible<byte[], T> {
    public static <T> @NotNull BytesMapper<T> newInstance() {
        return new BytesMapper<>();
    }

    @Override
    public @NotNull T forward(byte @NotNull [] bytes) {
        return EasyIo.deserialize(bytes);
    }

    @Override
    public byte @NotNull [] backward(@NotNull T instance) {
        return EasyIo.serialize(instance);
    }
}
