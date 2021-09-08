package io.webby.db.codec;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionCodec;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.DefaultUserCodec;
import io.webby.perf.stats.impl.StatsManager;
import io.webby.util.LazyBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static io.webby.db.codec.Codecs.*;
import static io.webby.util.EasyCast.castAny;

// Scan for codecs
public class CodecProvider {
    private final Map<Class<?>, Codec<?>> map = ImmutableMap.of(
            Session.class, new SessionCodec(),
            DefaultUser.class, new DefaultUserCodec(),
            Integer.class, INT_CODEC,
            Long.class, LONG_CODEC,
            String.class, STRING_CODEC
    );

    @Inject private Settings settings;
    @Inject private StatsManager statsManager;

    private final LazyBoolean isTrackingCodecOn = new LazyBoolean(() ->
        settings.isProfileMode() && settings.getBoolProperty("perf.track.codec.enabled", true)
    );

    public <T> @Nullable Codec<T> getCodecOrNull(@NotNull Class<T> klass) {
        Codec<T> codec = castAny(map.get(klass));
        if (codec != null && isTrackingCodecOn.get()) {
            return new TrackingCodecAdapter<>(codec, statsManager.newCodecStatsListener());
        }
        return codec;
    }

    public <T> @NotNull Codec<T> getCodecOrDie(@NotNull Class<T> klass) {
        Codec<T> codec = getCodecOrNull(klass);
        assert codec != null : "No codec found for the class: %s".formatted(klass);
        return codec;
    }

    public static final Codec<Integer> INT_CODEC = new Codec<>() {
        @Override
        public @NotNull CodecSize size() {
            return CodecSize.fixed(Integer.BYTES);
        }

        @Override
        public int writeTo(@NotNull OutputStream output, @NotNull Integer instance) throws IOException {
            return writeInt32(instance, output);
        }

        @Override
        public @NotNull Integer readFrom(@NotNull InputStream input, int available) throws IOException {
            return readInt32(input);
        }
    };

    public static final Codec<Long> LONG_CODEC = new Codec<>() {
        @Override
        public @NotNull CodecSize size() {
            return CodecSize.fixed(Long.BYTES);
        }

        @Override
        public int writeTo(@NotNull OutputStream output, @NotNull Long instance) throws IOException {
            return writeLong64(instance, output);
        }

        @Override
        public @NotNull Long readFrom(@NotNull InputStream input, int available) throws IOException {
            return readLong64(input);
        }
    };

    public static final Codec<String> STRING_CODEC = new Codec<>() {
        @Override
        public @NotNull CodecSize size() {
            return CodecSize.minSize(4);
        }

        @Override
        public int writeTo(@NotNull OutputStream output, @NotNull String instance) throws IOException {
            return writeString(instance, output);
        }

        @Override
        public @NotNull String readFrom(@NotNull InputStream input, int available) throws IOException {
            return readString(input);
        }
    };
}
