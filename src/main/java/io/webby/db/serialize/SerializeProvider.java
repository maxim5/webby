package io.webby.db.serialize;

import com.google.common.collect.ImmutableMap;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionSerializer;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.DefaultUserSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.webby.util.EasyCast.castAny;

// Scan for serializers
public class SerializeProvider {
    private final Map<Class<?>, Serializer<?>> map =
            ImmutableMap.<Class<?>, Serializer<?>>builder()
                    .put(Session.class, new SessionSerializer())
                    .put(DefaultUser.class, new DefaultUserSerializer())
            .build();

    @Nullable
    public <T> Serializer<T> getSerializer(@NotNull Class<T> klass) {
        return castAny(map.get(klass));
    }
}
