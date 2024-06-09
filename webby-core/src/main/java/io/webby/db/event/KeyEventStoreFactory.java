package io.webby.db.event;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.cache.BackgroundCacheCleaner;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.codec.CodecSize;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import static io.webby.db.codec.standard.Codecs.*;
import static io.webby.util.base.EasyCast.castAny;

public class KeyEventStoreFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;
    @Inject private KeyValueFactory factory;
    @Inject private Lifetime lifetime;
    @Inject private BackgroundCacheCleaner cacheCleaner;

    public <K, E> @NotNull KeyEventStore<K, E> getEventStore(@NotNull EventStoreOptions<K, E> options) {
        int cacheSizeSoftLimit = settings.getIntProperty("db.event.store.cache.size.soft.limit", 1 << 16);
        int cacheSizeHardLimit = settings.getIntProperty("db.event.store.cache.size.hard.limit", 1 << 17);
        int flushBatchSize = settings.getIntProperty("db.event.store.flush.batch.size", 64);
        int averageSizePerKey = settings.getIntProperty("db.event.store.average.size", 10);

        Codec<List<E>> codec = getListCodec(options.value(), averageSizePerKey);
        KeyValueDb<K, List<E>> db = factory.getDb(
            DbOptions.<K, List<E>>of(options.name(), options.key(), castAny(List.class)).withCustomValueCodec(codec)
        );
        CachingKvdbEventStore<K, E> store =
            new CachingKvdbEventStore<>(db, options.compacter(), cacheSizeSoftLimit, cacheSizeHardLimit, flushBatchSize);
        cacheCleaner.register(options.name(), store);
        lifetime.onTerminate(store);
        return store;
    }

    private <E> @NotNull Codec<List<E>> getListCodec(@NotNull Class<E> value, long averageSizePerKey) {
        Codec<E> valueCodec = provider.getCodecOrDie(value);
        return new Codec<>() {
            @Override
            public @NotNull CodecSize size() {
                if (averageSizePerKey == -1 || valueCodec.size().numBytes() < 0) {
                    return CodecSize.minSize(INT32_SIZE);
                }
                return CodecSize.averageSize(averageSizePerKey * valueCodec.size().numBytes());
            }

            @Override
            public int sizeOf(@NotNull List<E> instance) {
                if (instance.isEmpty()) {
                    return INT32_SIZE;
                }
                if (valueCodec.size().isFixed()) {
                    return instance.size() * valueCodec.sizeOf(instance.getFirst()) + INT32_SIZE;
                }
                return instance.stream().mapToInt(valueCodec::sizeOf).sum() + INT32_SIZE;
            }

            @Override
            public int writeTo(@NotNull OutputStream output, @NotNull List<E> instance) throws IOException {
                writeInt32(instance.size(), output);
                int total = INT32_SIZE;
                for (E event : instance) {
                    total += valueCodec.writeTo(output, event);
                }
                return total;
            }

            @Override
            public @NotNull List<E> readFrom(@NotNull InputStream input, int available) throws IOException {
                int size = readInt32(input);
                ImmutableList.Builder<E> builder = ImmutableList.builder();
                for (int i = 0; i < size; i++) {
                    E event = valueCodec.readFrom(input, input.available());
                    builder.add(event);
                }
                return builder.build();
            }
        };
    }

    public interface Compacter<E> {
        @NotNull Collection<E> compactInMemory(@NotNull Collection<E> events);
    }
}
