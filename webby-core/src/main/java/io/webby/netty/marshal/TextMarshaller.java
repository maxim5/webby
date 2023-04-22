package io.webby.netty.marshal;

import io.webby.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static io.webby.util.base.Unchecked.rethrow;

public interface TextMarshaller {
    void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException;

    default @NotNull String writeString(@NotNull Object instance) {
        try (StringWriter writer = new StringWriter()) {
            writeChars(writer, instance);
            return writer.toString();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException;

    default <T> @NotNull T readString(@NotNull String str, @NotNull Class<T> klass) {
        try (StringReader reader = new StringReader(str)) {
            return readChars(reader, klass);
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default <T> @NotNull Reversible<String, T> toStringReversible(@NotNull Class<T> klass) {
        return new Reversible<>() {
            @Override
            public @NotNull T forward(@NotNull String str) {
                return readString(str, klass);
            }

            @Override
            public @NotNull String backward(@NotNull T t) {
                return writeString(t);
            }
        };
    }
}
