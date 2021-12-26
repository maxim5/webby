package io.webby.db.event;

import com.google.common.collect.ImmutableList;
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
import java.util.Collection;
import java.util.List;

import static io.webby.util.base.EasyCast.castAny;

public class KeyEventStoreFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;
    @Inject private KeyValueFactory factory;

    public <K, E> @NotNull KeyEventStore<K, E> getEventStore(@NotNull String name,
                                                             @NotNull Class<K> key,
                                                             @NotNull Class<E> value) {
        return getEventStore(name, key, value, list -> list);
    }

    public <K, E> @NotNull KeyEventStore<K, E> getEventStore(@NotNull String name,
                                                             @NotNull Class<K> key,
                                                             @NotNull Class<E> value,
                                                             @NotNull Compacter<E> compacter) {
        int cacheSizeSoftLimit = settings.getIntProperty("db.event.store.cache.size.soft.limit", 1 << 16);
        int cacheSizeHardLimit = settings.getIntProperty("db.event.store.cache.size.hard.limit", 1 << 17);
        int flushBatchSize = settings.getIntProperty("db.event.store.flush.batch.size", 64);
        int averageSizePerKey = settings.getIntProperty("db.event.store.average.size", 10);

        Codec<List<E>> codec = getListCodec(value, averageSizePerKey);
        DbOptions<K, List<E>> options = DbOptions.<K, List<E>>of(name, key, castAny(List.class)).withCustomValueCodec(codec);
        KeyValueDb<K, List<E>> db = factory.getDb(options);
        return new CachingKvdbEventStore<>(db, compacter, cacheSizeSoftLimit, cacheSizeHardLimit, flushBatchSize);
    }

    private <E> @NotNull Codec<List<E>> getListCodec(@NotNull Class<E> value, long averageSizePerKey) {
        Codec<E> valueCodec = provider.getCodecOrDie(value);
        return new Codec<>() {
            @Override
            public @NotNull CodecSize size() {
                if (averageSizePerKey == -1 || valueCodec.size().numBytes() < 0) {
                    return CodecSize.minSize(4);
                }
                return CodecSize.averageSize(averageSizePerKey * valueCodec.size().numBytes());
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

    public interface Compacter<E> {
        @NotNull Collection<E> compactInMemory(@NotNull Collection<E> events);
    }
}
