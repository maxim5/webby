package io.webby.url.caller;

import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.url.annotate.Marshal;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContentProviderFactory {
    @Inject private MarshallerFactory factory;

    public @Nullable ContentProvider getContentProvider(@NotNull EndpointOptions options, @NotNull Class<?> klass) {
        return getContentProvider(options.in(), klass);
    }

    public @Nullable ContentProvider getContentProvider(@Nullable Marshal marshal, @NotNull Class<?> klass) {
        if (marshal != null) {
            if (klass == ByteBuf.class) {  // support byte[] and ByteBuffer?
                return (byteBuf, charset) -> byteBuf;
            }
            Marshaller marshaller = factory.getMarshaller(marshal);
            return (byteBuf, charset) -> marshaller.withCustomCharset(charset).readByteBuf(byteBuf, klass);
        }
        return null;
    }
}
