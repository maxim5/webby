package io.webby.db.kv.sql;

import com.google.inject.Inject;
import io.webby.db.codec.Codec;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.db.model.BlobKv;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

public class SqlTableDbFactory extends BaseKeyValueFactory {
    @Inject private TableManager tableManager;

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options.name(), () -> {
            if (tableManager.hasMatchingTable(options.name(), options.key(), options.value())) {
                TableObj<K, V> table = tableManager.getMatchingTableOrDie(options.name(), options.key(), options.value());
                return new SqlTableDb<>(table);
            }

            if (tableManager.hasMatchingTable(BlobKv.DB_NAME, byte[].class, BlobKv.class)) {
                TableObj<byte[], BlobKv> blobTable = tableManager.getMatchingTableOrDie(BlobKv.DB_NAME, byte[].class, BlobKv.class);
                Codec<K> keyCodec = keyCodecOrDie(options);
                Codec<V> valueCodec = valueCodecOrDie(options);
                return new BlobTableDb<>(blobTable, options.name(), keyCodec, valueCodec);
            }

            throw new IllegalArgumentException(
                "Failed to find a matching SQL table for %s. ".formatted(options) +
                "In order to support any key-value pairs, make sure `BlobKv` is in the models list."
            );
        });
    }
}
