package io.webby.url.caller;

import com.google.inject.Inject;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.url.annotate.Marshal;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContentProviderFactory {
    @Inject private MarshallerFactory factory;

    @Nullable
    public ContentProvider getContentProvider(@NotNull EndpointOptions options, @NotNull Class<?> klass) {
        return getContentProvider(options.in(), klass);
    }

    @Nullable
    public ContentProvider getContentProvider(@Nullable Marshal marshal, @NotNull Class<?> klass) {
        if (marshal != null) {
            Marshaller marshaller = factory.getMarshaller(marshal);
            return (byteBuf, charset) -> marshaller.withCustomCharset(charset).readByteBuf(byteBuf, klass);
        }
        return null;
    }
}
