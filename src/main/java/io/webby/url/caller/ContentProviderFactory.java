package io.webby.url.caller;

import io.webby.url.annotate.Marshal;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContentProviderFactory {
    @Nullable
    public ContentProvider getContentProvider(@NotNull EndpointOptions options, @NotNull Class<?> type) {
        return getContentProvider(options.in(), type);
    }

    @Nullable
    public ContentProvider getContentProvider(@Nullable Marshal marshal, @NotNull Class<?> type) {
        if (marshal != null) {
            return switch (marshal) {
                case JSON -> new JsonContentProvider(type);
                case PROTOBUF_BINARY -> throw new UnsupportedOperationException();
                case PROTOBUF_JSON -> throw new UnsupportedOperationException();
                case AS_STRING -> new SimpleContentProvider();
            };
        }
        return null;
    }
}
