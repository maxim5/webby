package io.webby.ws.convert;

import org.jetbrains.annotations.NotNull;

public interface OutFrameConverterListener<M> {
    void onConverter(@NotNull OutFrameConverter<M> converter);
}
