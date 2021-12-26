package io.webby.db.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.codec.CodecSize;
import io.webby.db.codec.Codecs;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static io.webby.util.base.EasyCast.castAny;

public class KeyEventStoreFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;
    @Inject private KeyValueFactory factory;

    public <K, E> @NotNull KeyEventStore<K, E> getEventStore(@NotNull String name, @NotNull Class<K> key, @NotNull Class<E> value) {
        ListMultimap<K, E> cache = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
        Codec<List<E>> codec = getListCodec(value);
        DbOptions<K, List<E>> options = DbOptions.<K, List<E>>of(name, key, castAny(List.class)).withCustomValueCodec(codec);
        KeyValueDb<K, List<E>> db = factory.getDb(options);
        int maxCacheSizeBeforeFlush = settings.getIntProperty("db.event.store.max.cache.size", 1 << 16);
        int flushBatchSize = settings.getIntProperty("db.event.store.flush.batch.size", 64);
        return new CachingKvdbEventStore<>(cache, db, maxCacheSizeBeforeFlush, flushBatchSize);
    }

    private <E> @NotNull Codec<List<E>> getListCodec(@NotNull Class<E> value) {
        Codec<E> valueCodec = provider.getCodecOrDie(value);
        return new Codec<>() {
            @Override
            public @NotNull CodecSize size() {
                return CodecSize.minSize(4);
            }

            @Override
            public int sizeOf(@NotNull List<E> instance) {
                if (instance.isEmpty()) {
                    return 4;
                }
                if (valueCodec.size().isFixed()) {
                    return instance.size() * valueCodec.sizeOf(instance.get(0)) + 4;
                }
                return instance.stream().mapToInt(valueCodec::sizeOf).sum() + 4;
            }

            @Override
            public int writeTo(@NotNull OutputStream output, @NotNull List<E> instance) throws IOException {
                Codecs.writeInt32(instance.size(), output);
                int total = 4;
                for (E event : instance) {
                    total += valueCodec.writeTo(output, event);
                }
                return total;
            }

            @Override
            public @NotNull List<E> readFrom(@NotNull InputStream input, int available) throws IOException {
                int size = Codecs.readInt32(input);
                ImmutableList.Builder<E> builder = ImmutableList.builder();
                for (int i = 0; i < size; i++) {
                    E event = valueCodec.readFrom(input, input.available());
                    builder.add(event);
                }
                return builder.build();
            }
        };
    }
}
