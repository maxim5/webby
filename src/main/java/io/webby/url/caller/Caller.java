package io.webby.url.caller;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Caller {
    Object call(@NotNull CharSequence url, @NotNull Map<String, CharBuffer> variables) throws Exception;

    @NotNull
    Object method();
}
