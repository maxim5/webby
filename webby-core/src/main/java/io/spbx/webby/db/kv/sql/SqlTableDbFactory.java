package io.spbx.webby.db.kv.sql;

import com.google.inject.Inject;
import io.spbx.orm.api.TableObj;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import io.spbx.webby.db.model.BlobKv;
import io.spbx.webby.db.sql.TableManager;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.base.EasyExceptions.newIllegalArgumentException;

public class SqlTableDbFactory extends BaseKeyValueFactory {
    @Inject private TableManager tableManager;

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
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

            throw newIllegalArgumentException(
                "Failed to find a matching SQL table for %s. " +
                "In order to support any key-value pairs, make sure `BlobKv` is in the models list.",
                options
            );
        });
    }
}
