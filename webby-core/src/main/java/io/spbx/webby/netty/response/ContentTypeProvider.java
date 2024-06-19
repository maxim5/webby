package io.spbx.webby.netty.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface ContentTypeProvider {
    @Nullable String getContentType(@NotNull Path path);
}
