package io.webby.db.kv.sql;

import com.google.inject.Inject;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.db.model.BlobKv;
import io.webby.db.sql.TableManager;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;

public class SqlTableDbFactory extends BaseKeyValueFactory {
    @Inject private TableManager tableManager;
    @Inject private CodecProvider codecProvider;

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            if (tableManager.hasMatchingTable(name, key, value)) {
                TableObj<K, V> table = tableManager.getMatchingTableOrDie(name, key, value);
                return new SqlTableDb<>(table);
            }

            if (tableManager.hasMatchingTable(BlobKv.NAME, byte[].class, BlobKv.class)) {
                TableObj<byte[], BlobKv> blobTable = tableManager.getMatchingTableOrDie(BlobKv.NAME, byte[].class, BlobKv.class);
                Codec<K> keyCodec = codecProvider.getCodecOrDie(key);
                Codec<V> valueCodec = codecProvider.getCodecOrDie(value);
                return new BlobTableDb<>(blobTable, name, keyCodec, valueCodec);
            }

            throw new IllegalArgumentException(
                "Failed to find a matching SQL table for key=%s value=%s. ".formatted(key, value) +
                "In order to support any key-value pairs, make sure `BlobKv` is in the models list."
            );
        });
    }
}
