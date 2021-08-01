package io.webby.url.caller;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.webby.url.convert.ConversionError;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class JsonContentProvider implements ContentProvider {
    private final Gson gson;
    private final Class<?> klass;

    public JsonContentProvider(@NotNull Gson gson, @NotNull Class<?> klass) {
        this.gson = gson;
        this.klass = klass;
    }

    @Override
    public Object getContent(@NotNull ByteBuf byteBuf, @NotNull Charset charset) {
        try {
            return gson.fromJson(new InputStreamReader(new ByteBufInputStream(byteBuf), charset), this.klass);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ConversionError("Failed to parse JSON content", e);
        }
    }
}
