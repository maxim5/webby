package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Caller {
    Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception;

    @NotNull Object method();
}
