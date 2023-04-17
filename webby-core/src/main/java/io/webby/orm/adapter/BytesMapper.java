package io.webby.orm.adapter;

import io.webby.util.base.Unchecked;
import io.webby.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static io.webby.util.base.EasyCast.castAny;

public class BytesMapper<T> implements Reversible<byte[], T> {
    public static <T> @NotNull BytesMapper<T> newInstance() {
        return new BytesMapper<>();
    }

    @Override
    public @NotNull T forward(byte @NotNull [] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ObjectInputStream inputStream = new ObjectInputStream(byteStream)) {
            return castAny(inputStream.readObject());
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        } catch (ClassNotFoundException e) {
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public byte @NotNull [] backward(@NotNull T instance) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteStream)) {
            outputStream.writeObject(instance);
            return byteStream.toByteArray();
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }
}
