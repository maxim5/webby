package io.webby.netty.marshal;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

public class JsonMarshaller implements Marshaller {
    @Inject protected Gson gson;

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) {
        gson.toJson(instance, writer);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) {
        return gson.fromJson(reader, klass);
    }
}
