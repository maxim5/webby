package io.webby.url.caller;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.webby.url.validate.ValidationError;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class JsonContentProvider implements ContentProvider {
    private final Gson gson = new Gson();
    private final Class<?> clazz;

    public JsonContentProvider(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object getContent(@NotNull ByteBuf byteBuf, @NotNull Charset charset) {
        try {
            return gson.fromJson(new InputStreamReader(new ByteBufInputStream(byteBuf), charset), this.clazz);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ValidationError("Failed to parse JSON content", e);
        }
    }
}
