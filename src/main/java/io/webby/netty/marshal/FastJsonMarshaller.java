package io.webby.netty.marshal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public record FastJsonMarshaller(@NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public FastJsonMarshaller(@NotNull Charset charset) {
        this.charset = charset;
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new FastJsonMarshaller(charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) {
        try (SerializeWriter serializeWriter = new SerializeWriter(writer)) {
            new JSONSerializer(serializeWriter).write(instance);  // TODO: serialize config?
        }
    }

    @Override
    public @NotNull String writeString(@NotNull Object instance) {
        return JSON.toJSONString(instance);
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        output.write(JSON.toJSONBytes(instance));
    }

    @Override
    public byte @NotNull [] writeBytes(@NotNull Object instance) {
        return JSON.toJSONBytes(instance);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        return JSON.parseObject(CharStreams.toString(reader), klass);
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        return JSON.parseObject(input, charset, klass);
    }
}
