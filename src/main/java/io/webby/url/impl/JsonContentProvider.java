package io.webby.url.impl;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
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
        return gson.fromJson(new InputStreamReader(new ByteBufInputStream(byteBuf), charset), this.clazz);
    }
}
