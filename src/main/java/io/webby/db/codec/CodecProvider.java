package io.webby.db.codec;

import com.google.common.collect.ImmutableMap;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionCodec;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.DefaultUserCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static io.webby.db.codec.Codecs.*;
import static io.webby.util.EasyCast.castAny;

// Scan for codecs
// TODO: temp
public class CodecProvider {
    private final Map<Class<?>, Codec<?>> map = ImmutableMap.of(
            Session.class, new SessionCodec(),
            DefaultUser.class, new DefaultUserCodec(),
            Long.class, new Codec<Long>() {
                @Override
                public int writeTo(@NotNull OutputStream output, @NotNull Long instance) throws IOException {
                    return writeLong64(instance, output);
                }

                @Override
                public @NotNull Long readFrom(@NotNull InputStream input, int available) throws IOException {
                    return readLong64(input);
                }
            },
            String.class, new Codec<String>() {
                @Override
                public int writeTo(@NotNull OutputStream output, @NotNull String instance) throws IOException {
                    return writeString(instance, output);
                }

                @Override
                public @NotNull String readFrom(@NotNull InputStream input, int available) throws IOException {
                    return readString(input);
                }
            }
    );

    @Nullable
    public <T> Codec<T> getCodecFor(@NotNull Class<T> klass) {
        return castAny(map.get(klass));
    }
}
