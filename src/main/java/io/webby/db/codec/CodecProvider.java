package io.webby.db.codec;

import com.google.common.collect.ImmutableMap;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionCodec;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.DefaultUserCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.webby.util.EasyCast.castAny;

// Scan for codecs
public class CodecProvider {
    private final Map<Class<?>, Codec<?>> map =
            ImmutableMap.<Class<?>, Codec<?>>builder()
                    .put(Session.class, new SessionCodec())
                    .put(DefaultUser.class, new DefaultUserCodec())
            .build();

    @Nullable
    public <T> Codec<T> getCodecFor(@NotNull Class<T> klass) {
        return castAny(map.get(klass));
    }
}
