package io.webby.url.caller;

import io.webby.url.SerializeMethod;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContentProviderFactory {
    @Nullable
    public ContentProvider getContentProvider(@NotNull EndpointOptions options, @NotNull Class<?> type) {
        return getContentProvider(options.in(), type);
    }

    @Nullable
    public ContentProvider getContentProvider(@Nullable SerializeMethod method, @NotNull Class<?> type) {
        if (method != null) {
            return switch (method) {
                case JSON -> new JsonContentProvider(type);
                case PROTOBUF -> throw new UnsupportedOperationException();
                case AS_STRING -> new SimpleContentProvider();
            };
        }
        return null;
    }
}